package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import ir.msdehghan.plugins.ansible.extractor.ExtractDoc;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class AnsibleModuleDtoParsingTest {
    private final ObjectMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_EMPTY)
            .build();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void ideReaderRetainsExtractorOutput() throws Exception {
        Path input = temporaryFolder.newFile("plugins.json").toPath();
        Path output = temporaryFolder.newFolder("docs").toPath();
        Files.writeString(input, """
                {"acme.cloud.server": {
                  "doc": {
                    "module": "server", "collection": "acme.cloud",
                    "filename": "/collections/acme/cloud/plugins/modules/server.py",
                    "author": ["Example Author"],
                    "deprecated": {"why": "Old API.", "alternative": "Use M(acme.cloud.server2).",
                      "removed_in": "3.0.0", "removed_from_collection": "acme.cloud"},
                    "options": {
                      "networks": {"type": "list", "elements": "dict", "suboptions": {
                        "settings": {"type": "dict", "suboptions": {
                          "enabled": {"type": "bool", "default": false, "required": true},
                          "state": {"type": "str", "aliases": ["status"],
                            "choices": {"up": "Running", "down": "Stopped"},
                            "version_added_collection": "acme.cloud"}
                        }}
                      }}
                    }
                  },
                  "return": {"result": {"type": "dict", "contains": {"id": {"type": "str"}}}},
                  "examples": "- acme.cloud.server: {}\\n",
                  "metadata": {"status": [], "custom": {"a": 1}}
                },
                "acme.cloud.ping": {"doc": {"module": "ping", "collection": "acme.cloud"}}}
                """);
        ExtractDoc.main(new String[]{"--input", input.toString(), "--output", output.toString()});

        // The strict IDE reader must accept every generated file, including short-name aliases.
        try (var files = Files.list(output)) {
            for (Path file : files.filter(f -> f.toString().endsWith(".json")).toList()) {
                assertNotNull(file.toString(), AnsibleModuleType.READER.readValue(file.toFile()));
            }
        }

        AnsibleModuleDto dto = AnsibleModuleType.READER.readValue(output.resolve("server.json").toFile());
        assertEquals("server", dto.name);
        assertEquals("acme.cloud.server", dto.qualifiedName);
        assertEquals("acme.cloud", dto.collection);
        assertEquals("/collections/acme/cloud/plugins/modules/server.py", dto.filename);
        assertEquals("- acme.cloud.server: {}\n", dto.examples);
        assertEquals(1, dto.metadata.at("/custom/a").asInt());
        assertEquals(Set.of("result"), dto.returnFields);
        assertEquals("str", dto.returns.get("result").at("/contains/id/type").asText());
        assertEquals("Example Author", dto.documentation.get("author").get(0).asText());
        assertEquals("Use 'acme.cloud.server2'.", dto.deprecated.alternative);
        assertEquals("acme.cloud", dto.deprecated.documentation.get("removed_from_collection").asText());

        AnsibleModuleDto.Field networks = dto.fields.get("networks");
        assertEquals("dict", networks.elements);
        AnsibleModuleDto.Field settings = networks.suboptions.get("settings");
        assertEquals("dict", settings.type);
        AnsibleModuleDto.Field enabled = settings.suboptions.get("enabled");
        assertTrue(enabled.required);
        assertEquals("false", enabled.defaultValue);
        AnsibleModuleDto.Field state = settings.suboptions.get("state");
        assertEquals(List.of("status"), state.aliases);
        assertEquals(List.of("up", "down"), state.choices);
        assertEquals("Running", state.choiceDescriptions.get("up").asText());
        assertEquals("acme.cloud", state.documentation.get("version_added_collection").asText());

        assertNull(AnsibleModuleType.READER.<AnsibleModuleDto>readValue(output.resolve("ping.json").toFile()).fields);
    }

    @Test
    public void readsGeneratedNestedOptionsAndMetadata() throws Exception {
        var generated = new api.AnsibleModuleDto();
        generated.name = "server";
        generated.qualifiedName = "acme.cloud.server";
        generated.collection = "acme.cloud";
        generated.filename = "/collections/acme/cloud/plugins/modules/server.py";
        generated.returnFields = Set.of("result");
        generated.returns = Map.of("result", mapper.readTree("""
                {"type": "dict", "contains": {"id": {"type": "str"}}}
                """));
        generated.examples = "- acme.cloud.server: {}\n";
        generated.metadata = mapper.readTree("{\"status\": [], \"supported_by\": \"community\"}");
        generated.documentation = Map.of("author", mapper.readTree("[\"Example Author\"]"));
        generated.deprecated = new api.AnsibleModuleDto.Deprecated();
        generated.deprecated.documentation = Map.of("removed_at_date", mapper.readTree("\"2027-01-01\""));

        var state = new api.AnsibleModuleDto.Field();
        state.type = "str";
        state.aliases = List.of("status");
        state.choices = List.of("up", "down");
        state.choiceDescriptions = mapper.readTree("{\"up\": \"Running\", \"down\": \"Stopped\"}");
        state.documentation = Map.of("version_added_collection", mapper.readTree("\"acme.cloud\""));
        var networks = new api.AnsibleModuleDto.Field();
        networks.type = "list";
        networks.elements = "dict";
        networks.suboptions = Map.of("state", state);
        generated.fields = Map.of("networks", networks);

        AnsibleModuleDto parsed = mapper.readValue(mapper.writeValueAsBytes(generated), AnsibleModuleDto.class);

        assertEquals(generated.qualifiedName, parsed.qualifiedName);
        assertEquals(generated.collection, parsed.collection);
        assertEquals(generated.filename, parsed.filename);
        assertEquals(generated.returns, parsed.returns);
        assertEquals(generated.returnFields, parsed.returnFields);
        assertEquals(generated.examples, parsed.examples);
        assertEquals(generated.metadata, parsed.metadata);
        assertEquals(generated.documentation, parsed.documentation);
        assertEquals(generated.deprecated.documentation, parsed.deprecated.documentation);
        assertEquals("dict", parsed.fields.get("networks").elements);
        AnsibleModuleDto.Field parsedState = parsed.fields.get("networks").suboptions.get("state");
        assertEquals(state.aliases, parsedState.aliases);
        assertEquals(state.choices, parsedState.choices);
        assertEquals(state.choiceDescriptions, parsedState.choiceDescriptions);
        assertEquals(state.documentation, parsedState.documentation);
    }

    @Test
    public void keepsFieldsAbsentForModulesWithoutOptions() throws Exception {
        var generated = new api.AnsibleModuleDto();
        generated.name = "service_facts";
        byte[] json = mapper.writeValueAsBytes(generated);

        assertFalse(mapper.readTree(json).has("fields"));
        assertNull(mapper.readValue(json, AnsibleModuleDto.class).fields);
    }
}
