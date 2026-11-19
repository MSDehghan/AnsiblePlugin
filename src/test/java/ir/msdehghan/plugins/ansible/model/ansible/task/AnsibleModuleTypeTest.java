package ir.msdehghan.plugins.ansible.model.ansible.task;

import org.junit.Test;

import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.MAPPING;
import static org.junit.Assert.*;

public class AnsibleModuleTypeTest {
    @Test
    public void shortAndQualifiedNamesShareModuleType() {
        AnsibleModuleField shortName = (AnsibleModuleField) AnsibleTask.TYPE.getFieldByName("copy").orElseThrow();
        AnsibleModuleField qualifiedName = (AnsibleModuleField) AnsibleTask.TYPE.getFieldByName("ansible.builtin.copy")
                .orElseThrow();

        assertSame(shortName.getType(MAPPING), qualifiedName.getType(MAPPING));
        assertEquals("ansible.builtin.copy", ((AnsibleModuleType) shortName.getType(MAPPING)).getModuleDto().name);
        assertEquals("ansible.builtin.copy", ((AnsibleModuleType) qualifiedName.getType(MAPPING)).getModuleDto().name);
    }

    @Test
    public void loadsModulesWithoutOptions() {
        AnsibleModuleType type = new AnsibleModuleType("test_module_without_options");
        assertTrue(type.getFields().isEmpty());
        assertFalse(type.getFieldByName("src").isPresent());
    }
}
