package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class AnsibleModuleType extends YamlType implements MappingType {
    // Strict so generated properties missing from AnsibleModuleDto fail instead of being dropped.
    static final ObjectReader READER = new ObjectMapper().readerFor(AnsibleModuleDto.class);
    private volatile ModuleData moduleData;

    private record ModuleData(AnsibleModuleDto dto, List<YamlField> fields) {
    }

    public AnsibleModuleType(String canonicalName) {
        super(canonicalName);
    }

    private static YamlField convertToYamlField(Map.Entry<String, AnsibleModuleDto.Field> field) {
        return new AnsibleModuleOptionField(field.getKey(), field.getValue());
    }

    public AnsibleModuleDto getModuleDto() {
        return getModuleData().dto();
    }

    private ModuleData getModuleData() {
        ModuleData data = moduleData;
        return data == null ? load() : data;
    }

    private synchronized ModuleData load() {
        if (moduleData == null) {
            try (InputStream pluginInputStream = AnsibleTask.class.getResourceAsStream("/docs/" + name + ".json")) {
                if (pluginInputStream == null) {
                    throw new IllegalStateException("Ansible Module " + name + " not found. it's Unexpected!");
                }
                AnsibleModuleDto loadedModuleDto = READER.readValue(pluginInputStream);
                if (loadedModuleDto == null) {
                    throw new IllegalStateException("Ansible Module " + name + " can't be loaded. it's Unexpected!");
                }
                // Modules without options have no fields.
                List<YamlField> loadedFields = loadedModuleDto.fields == null ? List.of() : loadedModuleDto.fields.entrySet().stream()
                        .map(AnsibleModuleType::convertToYamlField).toList();
                moduleData = new ModuleData(loadedModuleDto, loadedFields);
            } catch (IOException e) {
                throw new IllegalStateException("Can't read ansible module " + name, e);
            }
        }
        return moduleData;
    }

    @Override
    public List<YamlField> getFields() {
        return getModuleData().fields();
    }

    @Override
    public Optional<YamlField> getFieldByName(String name) {
        return getFields().stream().filter(field -> field.getName().equals(name)).findAny();
    }
}
