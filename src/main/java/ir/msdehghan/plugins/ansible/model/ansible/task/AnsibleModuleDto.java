package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.Set;

class AnsibleModuleDto {
    @JsonProperty("name")
    String name;

    @JsonProperty("qualifiedName")
    String qualifiedName;

    @JsonProperty("collection")
    String collection;

    @JsonProperty("filename")
    String filename;

    @JsonProperty("returns")
    Map<String, JsonNode> returns;

    @JsonProperty("examples")
    String examples;

    @JsonProperty("metadata")
    JsonNode metadata;

    @JsonProperty("documentation")
    Map<String, JsonNode> documentation;

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

        @JsonProperty("documentation")
        Map<String, JsonNode> documentation;

    }

    static class Field {
        @JsonProperty("aliases")
        List<String> aliases;

        @JsonProperty("choices")
        List<String> choices;

        @JsonProperty("choiceDescriptions")
        JsonNode choiceDescriptions;

        @JsonProperty("suboptions")
        Map<String, Field> suboptions;

        @JsonProperty("documentation")
        Map<String, JsonNode> documentation;

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
