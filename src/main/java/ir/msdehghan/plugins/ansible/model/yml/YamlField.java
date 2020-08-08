package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.documentation.DocumentationMarkup;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;

import java.util.EnumMap;
import java.util.stream.Collectors;

public class YamlField {
    private String name;
    private boolean required = false;
    private boolean deprecated = false;
    private String description = null;
    private Relation defaultValueRelation = Relation.Scalar;
    private final EnumMap<Relation, YamlType> valueTypeMap = new EnumMap<>(Relation.class);

    public YamlField(String name) {
        this.name = name;
    }

    public static YamlField create(String name) {
        return new YamlField(name);
    }

    public LookupElementBuilder getLookupElement() {
        String type;
        if (valueTypeMap.size() == 1) {
            type = getDefaultType().getName();
        } else {
            type = valueTypeMap.entrySet().stream().map(e -> e.getValue().getName() + "(" + e.getKey().name() + ")")
                    .collect(Collectors.joining("/"));
        }
        return LookupElementBuilder.create(this, name).withIcon(AllIcons.Json.Object).withBoldness(required)
                .withStrikeoutness(deprecated).withTypeText(type, true);
    }

    public String getName() {
        return name;
    }

    public YamlField setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public YamlField setRequired() {
        this.required = true;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public YamlField setDeprecated() {
        this.deprecated = true;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public YamlField setDescription(String description) {
        this.description = description;
        return this;
    }

    public YamlType getDefaultType() {
        return valueTypeMap.get(defaultValueRelation);
    }

    public YamlField setType(YamlType type) {
        valueTypeMap.put(defaultValueRelation, type);
        return this;
    }

    public YamlField setType(Relation relation, YamlType type) {
        return setType(relation, type, false);
    }

    public YamlField setType(Relation relation, YamlType type, boolean setAsDefault) {
        if (setAsDefault) defaultValueRelation = relation;
        valueTypeMap.put(relation, type);
        return this;
    }

    public YamlType getType(Relation relation) {
        return valueTypeMap.get(relation);
    }

    public String generateDoc() {
        final String NEW_LINE = "<br/>";
        StringBuilder sb = new StringBuilder(DocumentationMarkup.CONTENT_START);
        if (description != null && !description.isEmpty()) {
            sb.append(description).append(NEW_LINE);
        }
        if (required) {
            sb.append("<b>").append("This field is required").append("</b>").append(NEW_LINE);
        }
        if (deprecated) {
            sb.append("<b><i>").append("Deprecated").append("</i></b>").append(NEW_LINE);
        }

        String types = valueTypeMap.entrySet().stream().map(e -> e.getValue().getName() + "(" + e.getKey().name() + ")")
                .collect(Collectors.joining("/"));

        sb.append("Possible values: ").append(types).append(NEW_LINE);
        return sb.append(DocumentationMarkup.CONTENT_END).toString();
    }

    public enum Relation {
        Sequence, Mapping, Scalar
    }
}
