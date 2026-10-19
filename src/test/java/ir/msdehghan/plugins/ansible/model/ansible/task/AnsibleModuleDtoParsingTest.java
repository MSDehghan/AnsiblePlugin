package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class AnsibleModuleDtoParsingTest {
    private final ObjectMapper mapper = JsonMapper.builder()
            .serializationInclusion(JsonInclude.Include.NON_EMPTY)
            .build();

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
