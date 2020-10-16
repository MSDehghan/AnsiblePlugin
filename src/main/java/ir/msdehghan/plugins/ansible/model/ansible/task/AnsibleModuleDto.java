package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;
import java.util.Set;

class AnsibleModuleDto {
    @JsonProperty("name")
    String name;

    @JsonProperty("category")
    String category;

    @JsonProperty("returnFields")
    Set<String> returnFields;

    @JsonProperty("addedIn")
    String addedIn;

    @JsonProperty("shortDescription")
    String shortDescription;

    @JsonProperty("description")
    String description;

    @JsonProperty("deprecated")
    Deprecated deprecated;

    @JsonProperty("requirements")
    String requirements;

    @JsonProperty("notes")
    String notes;

    @JsonProperty("fields")
    Map<String, Field> fields;


    static class Deprecated {
        @JsonProperty("alternative")
        String alternative;

        @JsonProperty("why")
        String why;

        @JsonProperty("removedIn")
        String removedIn;

    }

    static class Field {
        @JsonProperty("aliases")
        List<String> aliases;

        @JsonProperty("choices")
        List<String> choices;

        @JsonProperty("description")
        String description;

        @JsonProperty("elements")
        String elements;

        @JsonProperty("required")
        boolean required;

        @JsonProperty("type")
        String type;

        @JsonProperty("versionAdded")
        String versionAdded;

        @JsonProperty("defaultValue")
        String defaultValue;
    }
}
