package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.intellij.codeInsight.completion.PrioritizedLookupElement;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.documentation.DocumentationMarkup;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlEnumType;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;

public class AnsibleModuleOptionField implements YamlField {
    private final String name;
    private final AnsibleModuleDto.Field field;
    private final YamlType type;
    private final Relation relation;

    public AnsibleModuleOptionField(String name, AnsibleModuleDto.Field field) {
        this.name = name;
        this.field = field;
        this.type = computeType();
        this.relation = computeRelation();
    }

    private YamlType computeType() {
        if (field.choices != null && !field.choices.isEmpty()) {
            return YamlEnumType.of(field.choices.toArray(new String[0]));
        } else if (field.type == null) {
            return YamlTypes.ANY;
        } else if (field.type.equals("list") && field.elements != null) {
            return getTypeFor(field.elements);
        }
        return getTypeFor(field.type);
    }

    private YamlType getTypeFor(String type) {
        if (type == null) return YamlTypes.ANY;
        switch (type) {
            case "str":
            case "raw":
            case "json":
                return YamlTypes.STRING;
            case "path":
                return YamlTypes.PATH;
            case "int":
                return YamlTypes.INTEGER;
            case "bool":
                return YamlTypes.BOOLEAN;
            case "float":
                return YamlTypes.FLOAT;
            default:
                return YamlTypes.ANY;
        }
    }

    private Relation computeRelation() {
        if (field.type == null) return Relation.SCALAR;
        switch (field.type) {
            case "dict":
                return Relation.MAPPING;
            case "list":
                return Relation.SEQUENCE;
            default:
                return Relation.SCALAR;
        }
    }

    @Override
    public LookupElement getLookupElement() {
        LookupElementBuilder lookupElement = LookupElementBuilder.create(this, name)
                .withIcon(AnsibleUtil.getIcon(relation))
                .withBoldness(field.required)
                .withTypeText(AnsibleUtil.getTypeText(relation, type));
        if (field.required) {
            return PrioritizedLookupElement.withPriority(lookupElement, 3);
        }
        return lookupElement;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public YamlType getType(Relation relation) {
        return relation == this.relation ? type : null;
    }

    @Override
    public String generateDoc() {
        final String NEW_LINE = "<br/>";
        StringBuilder sb = new StringBuilder(DocumentationMarkup.CONTENT_START);
        if (field.description != null && !field.description.isEmpty()) {
            sb.append(field.description).append(NEW_LINE);
        }
        if (field.required) {
            sb.append("<b>Required</b>");
        }
        sb.append(DocumentationMarkup.CONTENT_END);

        sb.append(DocumentationMarkup.SECTIONS_START);
        AnsibleUtil.appendSection("Default", field.defaultValue, sb);
        if (field.aliases != null) {
            AnsibleUtil.appendSection("Aliases", field.aliases.toString(), sb);
        }
        if (field.choices != null) {
            AnsibleUtil.appendSection("Choices", field.choices.toString(), sb);
        }
        AnsibleUtil.appendSection("Type", field.type, sb);
        AnsibleUtil.appendSection("Added in", field.versionAdded, sb);
        sb.append(DocumentationMarkup.SECTIONS_END);
        return sb.toString();
    }
}
