package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.documentation.DocumentationMarkup;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;
import org.jetbrains.annotations.NotNull;

import java.util.EnumMap;
import java.util.stream.Collectors;

import static ir.msdehghan.plugins.ansible.AnsibleUtil.getIcon;
import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.Scalar;

public class DefaultYamlField implements YamlField {
    private String name;
    private boolean required = false;
    private boolean deprecated = false;
    private String description = null;
    private Relation defaultValueRelation = null;
    private final EnumMap<Relation, YamlType> valueTypeMap = new EnumMap<>(Relation.class);

    public DefaultYamlField(@NotNull String name) {
        this.name = name;
    }

    public static DefaultYamlField create(String name) {
        return new DefaultYamlField(name);
    }

    @Override
    public LookupElement getLookupElement() {
        String type;
        if (valueTypeMap.size() == 1) {
            type = AnsibleUtil.getTypeText(defaultValueRelation, getDefaultType());
        } else {
            type = valueTypeMap.entrySet().stream().map(e -> AnsibleUtil.getTypeText(e.getKey(), e.getValue()))
                    .collect(Collectors.joining("/"));
        }
        LookupElementBuilder lookupElement = LookupElementBuilder.create(this, name)
                .withIcon(getIcon(defaultValueRelation))
                .withBoldness(required)
                .withStrikeoutness(deprecated)
                .withTypeText(type, true);

        if (required) {
            return PrioritizedLookupElement.withPriority(lookupElement, 3);
        }
        return lookupElement;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultYamlField setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isRequired() {
        return required;
    }

    public DefaultYamlField setRequired() {
        this.required = true;
        return this;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public DefaultYamlField setDeprecated() {
        this.deprecated = true;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public DefaultYamlField setDescription(String description) {
        this.description = description;
        return this;
    }

    @NotNull
    public YamlType getDefaultType() {
        return valueTypeMap.get(defaultValueRelation);
    }

    /**
     * Creates a Scalar relation with given type
     * @param type type of field
     * @return created field
     */
    public DefaultYamlField setType(@NotNull YamlType type) {
        return setType(Scalar, type);
    }

    public DefaultYamlField setType(@NotNull Relation relation,@NotNull YamlType type) {
        if (defaultValueRelation == null) defaultValueRelation = relation;
        valueTypeMap.put(relation, type);
        return this;
    }

    @Override
    @NotNull
    public YamlType getType(Relation relation) {
        return valueTypeMap.get(relation);
    }

    @Override
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

}
