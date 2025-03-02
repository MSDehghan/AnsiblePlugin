package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class AnsibleModuleType extends YamlType implements MappingType {
    private static final ObjectReader READER = new ObjectMapper().readerFor(AnsibleModuleDto.class);
    private AnsibleModuleDto moduleDto;
    private List<YamlField> fields;
    private List<YamlField> fieldsRO;


    public AnsibleModuleType(String name) {
        super(name);
    }

    private static YamlField convertToYamlField(Map.Entry<String, AnsibleModuleDto.Field> field) {
        return new AnsibleModuleOptionField(field.getKey(), field.getValue());
    }

    public AnsibleModuleDto getModuleDto() {
        ensureLoaded();
        return moduleDto;
    }

    private void ensureLoaded() {
        if (moduleDto == null) {
            load();
        }
    }

    private synchronized void load() {
        if (moduleDto == null) {
            try (InputStream pluginInputStream = AnsibleTask.class.getResourceAsStream("/docs/" + name + ".json")) {
                if (pluginInputStream == null) {
                    throw new IllegalStateException("Ansible Module " + name + " not found. it's Unexpected!");
                }
                moduleDto = READER.readValue(pluginInputStream);
                if (moduleDto == null) {
                    throw new IllegalStateException("Ansible Module " + name + " can't be loaded. it's Unexpected!");
                }
                fields = moduleDto.fields.entrySet().stream().map(AnsibleModuleType::convertToYamlField)
                        .collect(Collectors.toList());
                fieldsRO = Collections.unmodifiableList(fields);
            } catch (IOException e) {
                throw new IllegalStateException("Can't read ansible module " + name, e);
            }
        }
    }

    @Override
    public List<YamlField> getFields() {
        ensureLoaded();
        return fieldsRO;
    }

    @Override
    public Optional<YamlField> getFieldByName(String name) {
        ensureLoaded();
        if (fields != null) {
            return fields.stream().filter(field -> field.getName().equals(name)).findAny();
        }
        return Optional.empty();
    }
}
