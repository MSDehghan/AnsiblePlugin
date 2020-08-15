package ir.msdehghan.plugins.ansible.model.ansible.task;

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.lang.documentation.DocumentationMarkup;
import ir.msdehghan.plugins.ansible.AnsibleUtil;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;
import ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField;

public class AnsibleModuleField implements YamlField {
    private final String name;
    private final AnsibleModuleType moduleType;

    public AnsibleModuleField(String name) {
        this.name = name;
        this.moduleType = new AnsibleModuleType(name);
    }

    @Override
    public LookupElement getLookupElement() {
        return LookupElementBuilder.create(this, name)
                .withIcon(AllIcons.Nodes.Method)
                .withTypeText("Ansible Module");
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public YamlType getType(Relation relation) {
        switch (relation) {
            case Mapping:
                return moduleType;
            case Scalar:
                return YamlTypes.STRING;
            case Sequence:
            default:
                return null;
        }
    }

    @Override
    public String generateDoc() {
        AnsibleModuleDto moduleDto = moduleType.getModuleDto();
        final String NEW_LINE = "<br/>";
        StringBuilder sb = new StringBuilder(DocumentationMarkup.CONTENT_START);
        sb.append("<b> Module ").append(moduleDto.name).append("</b>").append(NEW_LINE);
        if (moduleDto.shortDescription != null && !moduleDto.shortDescription.isEmpty()) {
            sb.append("<p>").append(moduleDto.shortDescription).append("</p>");
        }
        sb.append(DocumentationMarkup.CONTENT_END);

        if (moduleDto.deprecated != null) {
            sb.append("<hr/>");
            sb.append("<div class='centered'><b>Deprecated</b></div>");
            sb.append(DocumentationMarkup.SECTIONS_START);
            AnsibleUtil.appendSection("Why", moduleDto.deprecated.why, sb);
            AnsibleUtil.appendSection("Removed in", moduleDto.deprecated.removedIn, sb);
            AnsibleUtil.appendSection("Alternative", moduleDto.description, sb);
            sb.append(DocumentationMarkup.SECTIONS_END);
            sb.append("<hr/>");
        }

        if (moduleDto.description != null && !moduleDto.description.isEmpty()) {
            sb.append(DocumentationMarkup.CONTENT_START)
                    .append(moduleDto.description)
                    .append(DocumentationMarkup.CONTENT_END);
        }
        sb.append(DocumentationMarkup.SECTIONS_START);
        AnsibleUtil.appendSection("Notes", moduleDto.notes, sb);
        AnsibleUtil.appendSection("Requirements", moduleDto.requirements, sb);
        if (moduleDto.returnFields != null) {
            AnsibleUtil.appendSection("Return fields", moduleDto.returnFields.toString(), sb);
        }
        AnsibleUtil.appendSection("Category", moduleDto.category, sb);
        AnsibleUtil.appendSection("Added in", moduleDto.addedIn, sb);
        sb.append(DocumentationMarkup.SECTIONS_END);
        return sb.toString();
    }
}
