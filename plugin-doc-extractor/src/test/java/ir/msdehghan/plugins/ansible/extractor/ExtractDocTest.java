package ir.msdehghan.plugins.ansible.extractor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class ExtractDocTest {
    private final ObjectMapper mapper = ExtractDoc.MAPPER;

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
    public void stripsMarkupFromDeprecation() throws Exception {
        JsonNode result = convert("""
                {"old": {"doc": {"module": "old", "deprecated": {
                  "why": "Uses API I(v1).", "alternative": "Use M(ansible.builtin.uri) instead.", "removed_in": "2.0.0"
                }}}}
                """, "old");
        assertEquals("Uses API 'v1'.", result.at("/deprecated/why").asText());
        assertEquals("Use 'ansible.builtin.uri' instead.", result.at("/deprecated/alternative").asText());
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
        assertEquals(List.of("acme.cloud.copy", "ansible.builtin.copy", "other.cloud.copy"),
                List.copyOf(modules.keySet()));
        assertEquals(Set.of("copy"), modules.get("ansible.builtin.copy").aliases());
        assertTrue(modules.get("acme.cloud.copy").aliases().isEmpty());
        assertTrue(modules.get("other.cloud.copy").aliases().isEmpty());

        input.remove("ansible.builtin.copy");
        modules = ExtractDoc.convertAll(input, false);
        assertEquals(Set.of("copy"), modules.get("acme.cloud.copy").aliases());
        assertTrue(modules.get("other.cloud.copy").aliases().isEmpty());
    }

    @Test
    public void usesQualifiedInputKeyWhenCollectionMetadataIsAbsent() throws Exception {
        JsonNode result = convert("{\"acme.cloud.server\": {\"doc\": {\"module\": \"server\"}}}", "acme.cloud.server");
        assertEquals("acme.cloud.server", result.path("qualifiedName").asText());
    }

    @Test
    public void preservesQualifiedRedirectsWithoutInferredAliases() throws Exception {
        var modules = ExtractDoc.convertAll(read("""
                {"old.cloud.server": {"doc": {
                  "plugin_name": "new.cloud.server", "short_description": "Old redirect"
                }}, "alpha.cloud.server": {"doc": {
                  "plugin_name": "new.cloud.server", "short_description": "First redirect"
                }}, "legacy": {"doc": {"module": "legacy"}}}
                """), true);
        assertEquals(Set.of("new.cloud.server", "legacy"), modules.keySet());
        var server = modules.get("new.cloud.server");
        assertEquals(List.of("alpha.cloud.server", "old.cloud.server"), List.copyOf(server.aliases()));
        assertEquals("new.cloud.server", server.dto().qualifiedName);
        assertEquals("First redirect", server.dto().shortDescription);
        assertEquals("legacy", modules.get("legacy").dto().name);
        assertTrue(modules.get("legacy").aliases().isEmpty());
    }

    @Test
    public void canonicalNamesCannotBeShadowedByAliases() throws Exception {
        var modules = ExtractDoc.convertAll(read("""
                {"acme.cloud.copy": {"doc": {"plugin_name": "ansible.builtin.copy"}},
                 "old.cloud.copy": {"doc": {"plugin_name": "acme.cloud.copy"}},
                 "copy": {"doc": {"module": "copy"}}}
                """), false);
        assertEquals(Set.of("acme.cloud.copy", "ansible.builtin.copy", "copy"), modules.keySet());
        assertEquals(Set.of("old.cloud.copy"), modules.get("acme.cloud.copy").aliases());
        assertTrue(modules.get("ansible.builtin.copy").aliases().isEmpty());
        assertTrue(modules.get("copy").aliases().isEmpty());
    }

    @Test
    public void writesCanonicalFilesAndRemovesOnlyObsoleteIndexedFiles() throws Exception {
        Path input = temporaryFolder.newFile("plugins.json").toPath();
        Path outside = temporaryFolder.newFile("outside.json").toPath();
        Path output = temporaryFolder.newFolder("docs").toPath();
        Files.writeString(input, """
                {"ansible.builtin.copy": {"doc": {"module": "copy", "collection": "ansible.builtin",
                    "short_description": "Canonical", "options": {"free-form": {"type": "str"}}}},
                 "ansible.builtin.copied": {"doc": {"plugin_name": "ansible.builtin.copy"}},
                 "copy": {"doc": {"module": "copy", "collection": "ansible.builtin"}},
                 "ansible.builtin.import_playbook": {"doc": {"module": "import_playbook"}}}
                """);
        Path index = output.resolve("plugins_list.txt");
        Files.writeString(index, "ansible.builtin.copy\ncopy\nretired.module.old\told\n../outside\n");
        Files.writeString(output.resolve("ansible.builtin.copy.json"), "x".repeat(1000));
        Files.writeString(output.resolve("copy.json"), "x".repeat(1000));
        Files.writeString(output.resolve("retired.module.old.json"), "obsolete");
        Files.writeString(output.resolve("old.json"), "unrelated");
        String[] args = {"--input", input.toString(), "--output", output.toString()};
        ExtractDoc.main(args);

        assertEquals(List.of("ansible.builtin.copy\tansible.builtin.copied\tcopy"), Files.readAllLines(index));
        JsonNode canonical = mapper.readTree(output.resolve("ansible.builtin.copy.json").toFile());
        assertEquals("ansible.builtin.copy", canonical.path("name").asText());
        assertEquals("ansible.builtin.copy", canonical.path("qualifiedName").asText());
        assertEquals("Canonical", canonical.path("shortDescription").asText());
        assertFalse(canonical.has("fields"));
        try (var files = Files.list(output)) {
            assertEquals(List.of("ansible.builtin.copy.json", "old.json", "plugins_list.txt"),
                    files.map(p -> p.getFileName().toString()).sorted().toList());
        }
        assertTrue(Files.exists(outside));

        Files.writeString(input, "{}");
        ExtractDoc.main(args);
        assertEquals(List.of(), Files.readAllLines(index));
        assertFalse(Files.exists(output.resolve("ansible.builtin.copy.json")));
        assertEquals("unrelated", Files.readString(output.resolve("old.json")));
    }

    @Test
    public void ignoresLongMalformedNamesInPreviousIndex() throws Exception {
        Path input = temporaryFolder.newFile("plugins.json").toPath();
        Path output = temporaryFolder.newFolder("docs").toPath();
        String name = "café.tools.mod_Ⅻ-²";
        Files.writeString(input, "{\"" + name + "\": {\"doc\": {\"plugin_name\": \"" + name + "\"}}}");
        Path index = output.resolve("plugins_list.txt");
        Files.writeString(index, "segment.".repeat(10_000) + "/invalid\n");

        ExtractDoc.main(new String[]{"--input", input.toString(), "--output", output.toString(), "--fqcn"});

        assertEquals(List.of(name), Files.readAllLines(index));
        assertTrue(Files.isRegularFile(output.resolve(name + ".json")));
    }

    @Test
    public void handlesLongMarkupAndEscapedContent() {
        String content = "x".repeat(50_000);
        assertEquals("'" + content + "'", AnsibleModule.readDesc("C(" + content + ")"));
        assertEquals("C(" + content, AnsibleModule.readDesc("C(" + content));
        assertEquals("'path(value)' 'label' 'ref' 'stdout'", AnsibleModule.readDesc(
                "C(path\\(value\\)) L(label, https://example.com) R(ref, target) RV(stdout)"));
    }

    @Test
    public void normalizesDescriptionShapes() {
        assertNull(AnsibleModule.readDesc(null));
        assertEquals("", AnsibleModule.readDesc(List.of()));
        assertEquals("Use 'copy'.", AnsibleModule.readDesc(List.of("Use", "M(copy).")));
        assertEquals("123", AnsibleModule.readDesc(123));
    }

    @Test
    public void parsesLegacyModuleCategories() {
        Doc doc = new Doc();
        doc.setFilename("/usr/lib/ansible/modules/cloud/provider/module_name.py");
        assertEquals("cloud/provider", doc.category);
        for (String filename : new String[]{"/usr/lib/ansible/modules/copy.py",
                "/collections/acme/cloud/plugins/modules/server.py", "/ansible/modules/cloud/readme.txt", null}) {
            Doc other = new Doc();
            other.setFilename(filename);
            assertNull(other.category);
        }
    }

    @Test
    public void acceptsOptionsInAnyOrderAndRejectsMissingPaths() throws Exception {
        Path input = temporaryFolder.newFile("input.json").toPath();
        Path output = temporaryFolder.newFolder("output").toPath();
        Files.writeString(input, """
                {"ansible.builtin.copy": {"doc": {"module": "copy", "collection": "ansible.builtin"}}}
                """);
        ExtractDoc.main(new String[]{"--output", output.toString(), "--fqcn", "--input", input.toString()});
        assertEquals("ansible.builtin.copy", Files.readString(output.resolve("plugins_list.txt")));
        assertThrows(IllegalArgumentException.class, () -> ExtractDoc.main(new String[]{"--input"}));
        assertThrows(IllegalArgumentException.class, () -> ExtractDoc.main(new String[]{"--output"}));
    }

    private Map<String, AnsibleModule> read(String json) throws Exception {
        return mapper.readValue(json, new TypeReference<>() {});
    }

    private JsonNode convert(String json, String name) throws Exception {
        return mapper.valueToTree(ExtractDoc.convertAll(read(json), true).get(name).dto());
    }
}
