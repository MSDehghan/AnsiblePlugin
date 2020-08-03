package ir.msdehghan.plugins.ansible.model.yml.type;

import ir.msdehghan.plugins.ansible.model.yml.YamlField;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class YamlMappingType extends YamlType {
    private final List<YamlField> fields;
    private final List<YamlField> readOnlyFields;

    public YamlMappingType(String name, int initialSize) {
        super(name);
        fields = new ArrayList<>(initialSize);
        readOnlyFields = Collections.unmodifiableList(fields);
    }

    public YamlMappingType(String name) {
        this(name, 1);
    }

    public List<YamlField> getFields() {
        return readOnlyFields;
    }

    public Optional<YamlField> getFieldByName(String name) {
        return fields.stream().filter(field -> field.getName().equals(name)).findAny();
    }

    protected <T extends YamlField> T addField(@NotNull T field) {
        fields.add(field);
        return field;
    }

    protected YamlField addField(@NotNull String field) {
        return addField(YamlField.create(field));
    }
}
