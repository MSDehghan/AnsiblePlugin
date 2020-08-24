package ir.msdehghan.plugins.ansible.model.yml.type;

import ir.msdehghan.plugins.ansible.model.yml.DefaultYamlField;
import ir.msdehghan.plugins.ansible.model.yml.type.api.MappingType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class YamlMappingType extends YamlType implements MappingType {
    private final List<YamlField> fields;
    private final List<YamlField> readOnlyFields;

    public YamlMappingType(String name, int initialSize) {
        super(name);
        fields = new ArrayList<>(initialSize);
        readOnlyFields = Collections.unmodifiableList(fields);
    }

    @Override
    public List<YamlField> getFields() {
        return readOnlyFields;
    }

    @Override
    public Optional<YamlField> getFieldByName(String name) {
        return fields.stream().filter(field -> field.getName().equals(name)).findAny();
    }

    protected <T extends YamlField> T addField(@NotNull T field) {
        fields.add(field);
        return field;
    }

    protected DefaultYamlField addField(@NotNull String field) {
        return addField(DefaultYamlField.create(field));
    }
}
