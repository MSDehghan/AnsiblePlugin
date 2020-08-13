package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import ir.msdehghan.plugins.ansible.model.yml.DefaultYamlField;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;

public interface YamlField {
    LookupElementBuilder getLookupElement();

    String getName();

    YamlType getDefaultType();

    YamlType getType(DefaultYamlField.Relation relation);

    String generateDoc();

    enum Relation {
        Sequence, Mapping, Scalar
    }
}
