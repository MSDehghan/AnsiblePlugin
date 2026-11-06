package ir.msdehghan.plugins.ansible.model.ansible.task;

import org.junit.Test;

import static org.junit.Assert.*;

public class AnsibleModuleTypeTest {
    @Test
    public void loadsModulesWithoutOptions() {
        AnsibleModuleType type = new AnsibleModuleType("test_module_without_options");
        assertTrue(type.getFields().isEmpty());
        assertFalse(type.getFieldByName("src").isPresent());
    }
}
