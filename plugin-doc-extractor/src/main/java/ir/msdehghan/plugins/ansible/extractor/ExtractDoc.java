package ir.msdehghan.plugins.ansible.extractor;

import api.AnsibleModuleDto;
import api.AnsibleModuleDto.Field;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Converts the output of <code>ansible-doc -t module --json &lt;module names&gt;</code>
 * into per module json files and a plugins_list.txt inside ./docs.
 * <p>
 * Emits qualified names and short-name aliases, with ansible.builtin winning short-name collisions.
 * Pass <code>--fqcn</code> to emit only qualified names.
 */
public class ExtractDoc {
    private static final String BUILTIN_PREFIX = "ansible.builtin.";

    // import_playbook is modeled as a play field rather than a task module.
    private static final Set<String> SKIPPED_MODULES = Set.of("import_playbook");
    private static final Set<String> SKIPPED_FIELDS = Set.of("free-form");

    public static void main(String[] args) throws IOException {
        boolean fqcn = false;
        Path input = null;
        Path output = Paths.get("./docs");
        var arguments = List.of(args).iterator();
        while (arguments.hasNext()) {
            String option = arguments.next();
            switch (option) {
                case "--fqcn" -> fqcn = true;
                case "--input", "--output" -> {
                    if (!arguments.hasNext()) {
                        throw new IllegalArgumentException("Missing path for " + option);
                    }
                    Path path = Paths.get(arguments.next());
                    if (option.equals("--input")) input = path;
                    else output = path;
                }
                default -> throw new IllegalArgumentException("Unknown option: " + option);
            }
        }
        ObjectMapper mapper = new JsonMapper();
        Map<String, AnsibleModule> map;
        try (InputStream stream = input == null ? ExtractDoc.class.getResourceAsStream("/plugins.json")
                : Files.newInputStream(input)) {
            if (stream == null) {
                throw new IOException("Missing plugins.json; supply --input <path>.");
            }
            map = mapper.readValue(stream, new TypeReference<>() {});
        }
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

        Map<String, AnsibleModuleDto> modules = convertAll(map, fqcn);

        Files.createDirectories(output);
        for (AnsibleModuleDto value : modules.values()) {
            Files.write(output.resolve(value.name + ".json"), mapper.writeValueAsBytes(value),
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
        Files.writeString(output.resolve("plugins_list.txt"), String.join("\n", modules.keySet()),
                StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public static Map<String, AnsibleModuleDto> convertAll(Map<String, AnsibleModule> map, boolean fqcn) {
        Map<String, AnsibleModuleDto> result = new TreeMap<>();
        // Builtin modules go first so they win short name collisions with collections.
        List<Map.Entry<String, AnsibleModule>> entries = new ArrayList<>(map.entrySet());
        entries.removeIf(e -> e.getValue() == null || e.getValue().doc == null
                || SKIPPED_MODULES.contains(Doc.shortName(e.getValue().doc.fullName(e.getKey()))));
        entries.sort(Comparator
                .comparing((Map.Entry<String, AnsibleModule> e) -> !e.getValue().doc.fullName(e.getKey()).startsWith(BUILTIN_PREFIX))
                .thenComparing(Map.Entry::getKey));

        for (Map.Entry<String, AnsibleModule> entry : entries) {
            AnsibleModule m = entry.getValue();
            String fullName = m.doc.fullName(entry.getKey());
            String shortName = Doc.shortName(fullName);
            result.putIfAbsent(fullName, convert(m, fullName, fullName));
            // ansible-doc may resolve a redirect to another plugin_name. Keep the requested FQCN too.
            if (entry.getKey().contains(".") && !entry.getKey().equals(fullName)) {
                result.putIfAbsent(entry.getKey(), convert(m, entry.getKey(), fullName));
            }
            if (!fqcn && !result.containsKey(shortName)) {
                result.put(shortName, convert(m, shortName, fullName));
            }
        }
        return result;
    }

    private static AnsibleModuleDto convert(AnsibleModule m, String name, String qualifiedName) {
        AnsibleModuleDto moduleDto = new AnsibleModuleDto();
        Doc doc = m.doc;

        moduleDto.name = name;
        moduleDto.qualifiedName = qualifiedName;
        moduleDto.collection = doc.collection;
        moduleDto.filename = doc.filename;
        moduleDto.addedIn = doc.version_added;
        moduleDto.category = doc.category != null ? doc.category : doc.collection;
        moduleDto.description = doc.description;
        moduleDto.shortDescription = doc.short_description;
        moduleDto.notes = doc.notes;
        moduleDto.requirements = doc.requirements;
        moduleDto.returnFields = m.returnFields != null ? m.returnFields.keySet() : null;
        moduleDto.returns = m.returnFields;
        moduleDto.examples = m.examples;
        moduleDto.metadata = m.metadata;
        moduleDto.documentation = doc.documentation;

        if (doc.deprecated != null) {
            moduleDto.deprecated = new AnsibleModuleDto.Deprecated();
            moduleDto.deprecated.alternative = doc.deprecated.alternative;
            moduleDto.deprecated.removedIn = doc.deprecated.removed_in;
            moduleDto.deprecated.why = doc.deprecated.why;
            moduleDto.deprecated.documentation = doc.deprecated.documentation;
        }

        moduleDto.fields = convertOptions(doc.options);
        return moduleDto;
    }

    private static Map<String, Field> convertOptions(Map<String, Option> options) {
        if (options == null || options.isEmpty()) return Map.of();
        Map<String, Field> fields = new TreeMap<>();
        for (Map.Entry<String, Option> optionEntry : options.entrySet()) {
            Option option = optionEntry.getValue();
            if (option == null || SKIPPED_FIELDS.contains(optionEntry.getKey())) {
                continue;
            }
            Field field = new Field();
            field.aliases = option.aliases;
            field.choices = option.choices;
            field.choiceDescriptions = option.choiceDescriptions;
            field.defaultValue = option.defaultValue;
            field.description = option.description;
            field.elements = option.elements;
            field.required = option.required;
            field.type = option.type;
            field.versionAdded = option.version_added;
            field.suboptions = convertOptions(option.suboptions);
            field.documentation = option.documentation;
            fields.put(optionEntry.getKey(), field);
        }
        return fields;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class AnsibleModule {
    public JsonNode metadata;
    public String examples;
    public Doc doc;

    @JsonProperty("return")
    public Map<String, JsonNode> returnFields;

    @Override
    public String toString() {
        return "AnsibleModule{" +
                "\nmetadata=" + metadata +
                ",\n doc=" + doc +
                ",\n returnFields=" + returnFields +
                '}';
    }

    // Ansible markup: B(), C(), E(), I(), M(), O(), P(), U(), V(), L(text, url), R(text, ref), RV(), ... Parentheses can be escaped with a backslash.
    private static final Pattern MARKUP = Pattern.compile("\\s?(?<!\\w)(RV|[A-Z])\\(((?:\\\\.|[^)\\\\])*+)\\)");
    private static final Pattern ESCAPE = Pattern.compile("\\\\(.)");

    public static String readDesc(Object o) {
        if (o != null) {
            String text;
            if (o instanceof List<?> list) {
                text = String.join(" ", list.stream().map(String::valueOf).toList());
            } else {
                text = o.toString();
            }
            return MARKUP.matcher(text).replaceAll(r -> {
                String type = r.group(1);
                String content = r.group(2);
                if (type.equals("L") || type.equals("R")) {
                    // L(text, url) and R(text, ref): keep the text only.
                    int comma = content.indexOf(',');
                    if (comma >= 0) {
                        content = content.substring(0, comma);
                    }
                }
                content = ESCAPE.matcher(content.trim()).replaceAll("$1");
                return Matcher.quoteReplacement(" '" + content + "'");
            }).trim();
        }
        return null;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Doc {
    public String module;
    @JsonProperty("plugin_name")
    public String pluginName;
    public String collection;
    public String version_added;
    public String short_description;
    public String filename;
    public String category;
    public String description;
    public Deprecated deprecated;
    public String requirements;
    public String notes;
    public Map<String, Option> options;

    // Retain documentation metadata that is not needed by the normalized completion model.
    @JsonAnySetter
    public Map<String, JsonNode> documentation = new TreeMap<>();

    @JsonSetter("description")
    public void setDescription(Object o) {
        this.description = AnsibleModule.readDesc(o);
    }

    @JsonSetter("requirements")
    public void setRequirements(Object o) {
        this.requirements = AnsibleModule.readDesc(o);
    }

    @JsonSetter("notes")
    public void setNotes(Object o) {
        this.notes = AnsibleModule.readDesc(o);
    }

    // Only old style paths (ansible/modules/<category>/x.py) have a category, collections fall back to the collection name.
    private static final String MODULES_DIRECTORY = "ansible/modules/";
    private static final Pattern MODULE_FILENAME = Pattern.compile("\\w+\\.py");

    @JsonSetter("filename")
    public void setFilename(String filename) {
        this.filename = filename;
        category = null;
        if (filename != null) {
            int modulesIndex = filename.lastIndexOf(MODULES_DIRECTORY);
            int lastSlash = filename.lastIndexOf('/');
            if (modulesIndex >= 0 && lastSlash > modulesIndex + MODULES_DIRECTORY.length()
                    && MODULE_FILENAME.matcher(filename.substring(lastSlash + 1)).matches()) {
                category = filename.substring(modulesIndex + MODULES_DIRECTORY.length(), lastSlash);
            }
        }
    }

    /** Fully qualified name of the module, falling back to the key used in ansible-doc output. */
    public String fullName(String key) {
        if (pluginName != null && !pluginName.isBlank()) {
            return pluginName;
        }
        if (collection != null && module != null && !module.contains(".")) {
            return collection + "." + module;
        }
        if (key.contains(".")) {
            return key;
        }
        return module != null ? module : key;
    }

    public static String shortName(String fullName) {
        return fullName.substring(fullName.lastIndexOf('.') + 1);
    }

    @Override
    public String toString() {
        return "Doc{" +
                "module='" + module + '\'' +
                ", versionAdded='" + version_added + '\'' +
                ", shortDescription='" + short_description + '\'' +
                ", filename='" + filename + '\'' +
                ", description='" + description + '\'' +
                ", deprecated=" + deprecated +
                ", requirements=" + requirements +
                ", notes=" + notes +
                '}';
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Deprecated {
        public String alternative;
        public String why;
        public String removed_in;

        @JsonAnySetter
        public Map<String, JsonNode> documentation = new TreeMap<>();

        @Override
        public String toString() {
            return "Deprecated{" +
                    "alternative='" + alternative + '\'' +
                    ", why='" + why + '\'' +
                    ", removed_in='" + removed_in + '\'' +
                    '}';
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Option {
    public List<String> aliases;
    public List<String> choices;
    public String description;
    public String elements;
    public Map<String, Option> suboptions;
    public JsonNode choiceDescriptions;

    @JsonAnySetter
    public Map<String, JsonNode> documentation = new TreeMap<>();

    // Newer ansible-doc output may describe choices as a map of value to description.
    @JsonSetter("choices")
    public void setChoices(JsonNode o) {
        if (o == null || o.isNull()) {
            return;
        }
        List<String> result = new ArrayList<>();
        if (o.isObject()) {
            choiceDescriptions = o;
            o.fieldNames().forEachRemaining(result::add);
        } else if (o.isArray()) {
            o.forEach(n -> result.add(n.asText()));
        } else {
            result.add(o.asText());
        }
        choices = result;
    }

    public Boolean required;
    public String type;
    public String version_added;

    @JsonProperty("default")
    public String defaultValue;


    @JsonSetter("default")
    public void setDefaultValue(JsonNode o) {
        if (o != null) {
            defaultValue = o.toString();
        }
    }

    @JsonSetter("description")
    public void setDescription(Object o) {
        this.description = AnsibleModule.readDesc(o);
    }
}
