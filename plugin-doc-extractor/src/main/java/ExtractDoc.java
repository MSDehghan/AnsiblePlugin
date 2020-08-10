import api.AnsibleModuleDto;
import api.AnsibleModuleDto.Field;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtractDoc {
    public static void main(String[] args) throws IOException {
        ObjectMapper mapper = new JsonMapper();
        Map<String, AnsibleModule> map = mapper.readValue(ExtractDoc.class.getResource("/plugins.json"),
                new TypeReference<Map<String, AnsibleModule>>() {
                });
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        Files.createDirectories(Paths.get("./docs"));
        for (AnsibleModuleDto value : map.values().stream().map(ExtractDoc::convert).collect(Collectors.toList())) {
            Files.write(Paths.get("./docs", value.name + ".json"), mapper.writeValueAsBytes(value), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }

    }

    public static AnsibleModuleDto convert(AnsibleModule m) {
        AnsibleModuleDto moduleDto = new AnsibleModuleDto();
        Doc doc = m.doc;

        moduleDto.name = doc.module;
        moduleDto.addedIn = doc.version_added;
        moduleDto.category = doc.category;
        moduleDto.description = doc.description;
        moduleDto.shortDescription = doc.short_description;
        moduleDto.notes = doc.notes;
        moduleDto.requirements = doc.requirements;
        moduleDto.returnFields = m.returnFields != null ? m.returnFields.keySet() : null;

        if (doc.deprecated != null) {
            moduleDto.deprecated = new AnsibleModuleDto.Deprecated();
            moduleDto.deprecated.alternative = doc.deprecated.alternative;
            moduleDto.deprecated.removedIn = doc.deprecated.removed_in;
            moduleDto.deprecated.why = doc.deprecated.why;
        }

        if (doc.options != null) {
            moduleDto.fields = new HashMap<>(doc.options.size());
            for (Map.Entry<String, Option> optionEntry : doc.options.entrySet()) {
                Option option = optionEntry.getValue();
                Field field = new Field();
                field.aliases = option.aliases;
                field.choices = option.choices;
                field.defaultValue = option.defaultValue;
                field.description = option.description;
                field.elements = option.elements;
                field.required = option.required;
                field.type = option.type;
                field.versionAdded = option.version_added;
                moduleDto.fields.put(optionEntry.getKey(), field);
            }
        }
        return moduleDto;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class AnsibleModule {
    public MetaData metadata;
    public Doc doc;

    @JsonProperty("return")
    public Map<String, Object> returnFields;

    @Override
    public String toString() {
        return "AnsibleModule{" +
                "\nmetadata=" + metadata +
                ",\n doc=" + doc +
                ",\n returnFields=" + returnFields +
                '}';
    }

    public static String readDesc(Object o) {
        if (o != null) {
            String text;
            if (o instanceof List) {
                text = String.join(" ", (List<String>) o);
            } else {
                text = o.toString();
            }
            return text.replaceAll("\\s?[A-Z]\\((.+?)\\)", " '$1'");
        }
        return null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MetaData {
        public String status;
        public String supported_by;

        @JsonSetter("status")
        public void setStatus(List<String> o) {
            this.status = o.get(0);
        }

        @Override
        public String toString() {
            return "MetaData{" +
                    "status=" + status +
                    ", supported_by=" + supported_by +
                    '}';
        }
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Doc {
    public String module;
    public String version_added;
    public String short_description;
    public String filename;
    public String category;
    public String description;
    public Deprecated deprecated;
    public String requirements;
    public String notes;
    public Map<String, Option> options;

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

    @JsonSetter("filename")
    public void setFilename(String filename) {
        this.filename = filename;
        category = filename.replaceFirst("^.*ansible/modules/(.*?)/[a-zA-z0-9]+\\.py$", "$1");
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

    public static class Deprecated {
        public String alternative;
        public String why;
        public String removed_in;

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
    public Boolean required;
    public String type;
    public String version_added;

    @JsonProperty("default")
    public String defaultValue;


    @JsonSetter("default")
    public void setNotes(JsonNode o) {
        if (o != null) {
            defaultValue = o.toString();
        }
    }

    @JsonSetter("description")
    public void setDescription(Object o) {
        this.description = AnsibleModule.readDesc(o);
    }
}