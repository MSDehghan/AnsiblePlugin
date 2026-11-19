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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;

/**
 * Converts the output of <code>ansible-doc -t module --json &lt;module names&gt;</code>
 * into canonical module JSON files and a grouped plugins_list.txt inside ./docs.
 * <p>
 * Emits qualified names and short-name aliases, with ansible.builtin winning short-name collisions.
 * Pass <code>--fqcn</code> to omit inferred short-name aliases.
 */
public class ExtractDoc {
    private static final String BUILTIN_PREFIX = "ansible.builtin.";
    private static final Pattern MODULE_NAME_CHARACTERS = Pattern.compile("[\\p{L}\\p{N}_.-]+");

    // import_playbook is modeled as a play field rather than a task module.
    private static final Set<String> SKIPPED_MODULES = Set.of("import_playbook");
    private static final Set<String> SKIPPED_FIELDS = Set.of("free-form");

    static final ObjectMapper MAPPER = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_EMPTY)
            .build();

    public static void main(String[] args) throws IOException {
        Options options = Options.parse(args);
        writeModules(options.output(), convertAll(readModules(options.input()), options.fqcn()));
    }

    private record Options(boolean fqcn, Path input, Path output) {
        private static Options parse(String[] args) {
            boolean fqcn = false;
            Path input = null;
            Path output = Path.of("./docs");
            var arguments = List.of(args).iterator();
            while (arguments.hasNext()) {
                String option = arguments.next();
                switch (option) {
                    case "--fqcn" -> fqcn = true;
                    case "--input" -> input = path(arguments, option);
                    case "--output" -> output = path(arguments, option);
                    default -> throw new IllegalArgumentException("Unknown option: " + option);
                }
            }
            return new Options(fqcn, input, output);
        }

        private static Path path(Iterator<String> arguments, String option) {
            if (!arguments.hasNext()) {
                throw new IllegalArgumentException("Missing path for " + option);
            }
            return Path.of(arguments.next());
        }
    }

    private static Map<String, AnsibleModule> readModules(Path input) throws IOException {
        try (InputStream stream = input == null ? ExtractDoc.class.getResourceAsStream("/plugins.json")
                : Files.newInputStream(input)) {
            if (stream == null) {
                throw new IOException("Missing plugins.json; supply --input <path>.");
            }
            return MAPPER.readValue(stream, new TypeReference<>() {});
        }
    }

    private static void writeModules(Path output, Map<String, ModuleDocs> modules) throws IOException {
        Files.createDirectories(output);
        Path index = output.resolve("plugins_list.txt");
        List<String> previousRows = Files.isRegularFile(index, NOFOLLOW_LINKS) ? Files.readAllLines(index) : List.of();
        List<String> rows = new ArrayList<>();
        for (ModuleDocs module : modules.values()) {
            String name = module.dto().name;
            if (!isModuleName(name)) {
                throw new IOException("Invalid module name: " + name);
            }
            Files.write(output.resolve(name + ".json"), MAPPER.writeValueAsBytes(module.dto()));
            rows.add(module.aliases().isEmpty() ? name : name + "\t" + String.join("\t", module.aliases()));
        }
        for (String row : previousRows) {
            // Only the first column owned a JSON file, including in the legacy one-name-per-line index.
            String name = row.split("\t", 2)[0];
            if (!modules.containsKey(name) && isModuleName(name)) {
                Path obsolete = output.resolve(name + ".json");
                if (Files.isRegularFile(obsolete, NOFOLLOW_LINKS)) {
                    Files.deleteIfExists(obsolete);
                }
            }
        }
        Files.writeString(index, String.join("\n", rows));
    }

    private static boolean isModuleName(String name) {
        return !name.startsWith(".") && !name.endsWith(".") && !name.contains("..")
                && MODULE_NAME_CHARACTERS.matcher(name).matches();
    }

    record ModuleDocs(AnsibleModuleDto dto, Set<String> aliases) {}

    private record Entry(String key, AnsibleModule module, String fullName) {}

    static Map<String, ModuleDocs> convertAll(Map<String, AnsibleModule> map, boolean fqcn) {
        List<Entry> entries = sortedEntries(map);
        Map<String, Entry> sources = new TreeMap<>();
        for (Entry e : entries) {
            if (e.key().equals(e.fullName())) {
                sources.put(e.fullName(), e);
            } else {
                sources.putIfAbsent(e.fullName(), e);
            }
        }
        Map<String, ModuleDocs> result = new TreeMap<>();
        sources.forEach((name, entry) -> result.put(name, new ModuleDocs(convert(entry.module(), name), new TreeSet<>())));
        assignAliases(entries, result, fqcn);
        return result;
    }

    private static List<Entry> sortedEntries(Map<String, AnsibleModule> map) {
        List<Entry> entries = new ArrayList<>();
        map.forEach((key, module) -> {
            if (module != null && module.doc != null) {
                String fullName = module.doc.fullName(key);
                if (!SKIPPED_MODULES.contains(Doc.shortName(fullName))) {
                    entries.add(new Entry(key, module, fullName));
                }
            }
        });
        // Builtin modules go first so they win short name collisions with collections.
        entries.sort(Comparator.comparing((Entry e) -> !e.fullName().startsWith(BUILTIN_PREFIX))
                .thenComparing(Entry::key));
        return entries;
    }

    private static void assignAliases(List<Entry> entries, Map<String, ModuleDocs> modules, boolean fqcn) {
        Set<String> claimedNames = new HashSet<>(modules.keySet());
        for (Entry e : entries) {
            Set<String> aliases = modules.get(e.fullName()).aliases();
            // ansible-doc may resolve a redirect to another plugin_name. Keep the requested FQCN too.
            if (e.key().contains(".") && claimedNames.add(e.key())) {
                aliases.add(e.key());
            }
            if (!fqcn) {
                String shortName = Doc.shortName(e.fullName());
                if (claimedNames.add(shortName)) {
                    aliases.add(shortName);
                }
            }
        }
    }

    private static AnsibleModuleDto convert(AnsibleModule m, String canonicalName) {
        AnsibleModuleDto moduleDto = new AnsibleModuleDto();
        Doc doc = m.doc;

        moduleDto.name = canonicalName;
        moduleDto.qualifiedName = canonicalName;
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
            moduleDto.deprecated.alternative = AnsibleModule.readDesc(doc.deprecated.alternative);
            moduleDto.deprecated.removedIn = doc.deprecated.removed_in;
            moduleDto.deprecated.why = AnsibleModule.readDesc(doc.deprecated.why);
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

    // Ansible markup: B(), C(), E(), I(), M(), O(), P(), U(), V(), L(text, url), R(text, ref), RV(), ... Parentheses can be escaped with a backslash.
    private static final Pattern MARKUP = Pattern.compile("\\s?(?<!\\w)(RV|[A-Z])\\(((?:\\\\.|[^)\\\\])*+)\\)");
    private static final Pattern ESCAPE = Pattern.compile("\\\\(.)");

    public static String readDesc(Object o) {
        if (o == null) return null;
        String text = o instanceof List<?> list
                ? list.stream().map(String::valueOf).collect(Collectors.joining(" "))
                : o.toString();
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
    // Index arithmetic avoids regex backtracking across the whole path.
    private static final String MODULES_DIRECTORY = "ansible/modules/";
    private static final Pattern MODULE_FILENAME = Pattern.compile("\\w+\\.py");

    @JsonSetter("filename")
    public void setFilename(String filename) {
        this.filename = filename;
        category = null;
        if (filename == null) return;
        int start = filename.lastIndexOf(MODULES_DIRECTORY) + MODULES_DIRECTORY.length();
        int lastSlash = filename.lastIndexOf('/');
        if (start >= MODULES_DIRECTORY.length() && lastSlash > start
                && MODULE_FILENAME.matcher(filename.substring(lastSlash + 1)).matches()) {
            category = filename.substring(start, lastSlash);
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

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Deprecated {
        public String alternative;
        public String why;
        public String removed_in;

        @JsonAnySetter
        public Map<String, JsonNode> documentation = new TreeMap<>();
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
    public Boolean required;
    public String type;
    public String version_added;
    public String defaultValue;

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
