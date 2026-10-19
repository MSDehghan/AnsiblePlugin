import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.*;

public class ExtractDocTest {
    private final ObjectMapper mapper = new JsonMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void preservesNestedOptionsAndDocumentation() throws Exception {
        JsonNode result = convert("""
                {"acme.cloud.server": {
                  "doc": {
                    "module": "server", "collection": "acme.cloud",
                    "author": ["Example Author"], "attributes": {"check_mode": {"support": "full"}},
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
                  "return": {"result": {"type": "dict", "contains": {
                    "id": {"type": "str", "description": "Server identifier"}
                  }}},
                  "examples": "- acme.cloud.server: {}\\n",
                  "metadata": {"status": [], "supported_by": "community", "custom": {"a": 1}}
                }}
                """, "acme.cloud.server");

        String nested = "/fields/networks/suboptions/settings/suboptions/";
        assertTrue(result.at(nested + "enabled/required").asBoolean());
        assertEquals("false", result.at(nested + "enabled/defaultValue").asText());
        assertEquals("status", result.at(nested + "state/aliases/0").asText());
        assertEquals("up", result.at(nested + "state/choices/0").asText());
        assertEquals("Running", result.at(nested + "state/choiceDescriptions/up").asText());
        assertEquals("acme.cloud", result.at(nested + "state/documentation/version_added_collection").asText());
        assertEquals("str", result.at("/returns/result/contains/id/type").asText());
        assertEquals("- acme.cloud.server: {}\n", result.path("examples").asText());
        assertEquals(1, result.at("/metadata/custom/a").asInt());
        assertTrue(result.at("/metadata/status").isArray());
        assertEquals("Example Author", result.at("/documentation/author/0").asText());
        assertEquals("full", result.at("/documentation/attributes/check_mode/support").asText());
    }

    @Test
    public void omitsFieldsForModulesWithoutOptions() throws Exception {
        for (String options : new String[]{"", ", \"options\": {}", ", \"options\": null",
                ", \"options\": {\"free-form\": {\"type\": \"str\"}}"}) {
            JsonNode result = convert("{\"empty\": {\"doc\": {\"module\": \"empty\"" + options + "}}}", "empty");
            assertFalse(result.has("fields"));
        }
    }

    @Test
    public void preservesCollectionModulesWhenShortNamesCollide() throws Exception {
        Map<String, AnsibleModule> input = read("""
                {"other.cloud.copy": {"doc": {"module": "copy", "collection": "other.cloud"}},
                 "ansible.builtin.copy": {"doc": {"module": "copy", "collection": "ansible.builtin"}},
                 "acme.cloud.copy": {"doc": {"module": "copy", "collection": "acme.cloud"}}}
                """);
        var modules = ExtractDoc.convertAll(input, false);
        assertEquals(4, modules.size());
        assertEquals("ansible.builtin.copy", mapper.valueToTree(modules.get("copy")).path("qualifiedName").asText());
        assertTrue(modules.containsKey("other.cloud.copy"));
        assertTrue(modules.containsKey("acme.cloud.copy"));
        assertEquals(3, ExtractDoc.convertAll(input, true).size());
    }

    @Test
    public void usesQualifiedInputKeyWhenCollectionMetadataIsAbsent() throws Exception {
        JsonNode result = convert("{\"acme.cloud.server\": {\"doc\": {\"module\": \"server\"}}}", "acme.cloud.server");
        assertEquals("acme.cloud.server", result.path("qualifiedName").asText());
    }

    @Test
    public void preservesRedirectedInputNames() throws Exception {
        var modules = ExtractDoc.convertAll(read("""
                {"old.cloud.server": {"doc": {
                  "module": "server", "plugin_name": "new.cloud.server", "collection": "new.cloud"
                }}}
                """), true);
        assertEquals(2, modules.size());
        assertTrue(modules.containsKey("new.cloud.server"));
        assertEquals("new.cloud.server",
                mapper.valueToTree(modules.get("old.cloud.server")).path("qualifiedName").asText());
    }

    @Test
    public void writesMatchingFilesAndListAndTruncatesExistingFiles() throws Exception {
        Path input = temporaryFolder.newFile("plugins.json").toPath();
        Path output = temporaryFolder.newFolder("docs").toPath();
        Files.writeString(input, """
                {"ansible.builtin.copy": {"doc": {"module": "copy", "collection": "ansible.builtin"}},
                 "ansible.builtin.import_playbook": {"doc": {"module": "import_playbook"}}}
                """);
        Files.writeString(output.resolve("copy.json"), "x".repeat(1000));
        ExtractDoc.main(new String[]{"--input", input.toString(), "--output", output.toString()});

        assertEquals("ansible.builtin.copy\ncopy", Files.readString(output.resolve("plugins_list.txt")));
        assertEquals("copy", mapper.readTree(output.resolve("copy.json").toFile()).path("name").asText());
        assertTrue(Files.exists(output.resolve("ansible.builtin.copy.json")));
        assertFalse(Files.exists(output.resolve("import_playbook.json")));
    }

    private Map<String, AnsibleModule> read(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<>() {});
    }

    private JsonNode convert(String json, String name) throws Exception {
        return mapper.valueToTree(ExtractDoc.convertAll(read(json), true).get(name));
    }
}
