package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.codeInsight.lookup.LookupElement;
import ir.msdehghan.plugins.ansible.model.yml.DefaultYamlField;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;

public interface YamlField {
    LookupElement getLookupElement();

    String getName();

    YamlType getType(DefaultYamlField.Relation relation);

    String generateDoc();

    enum Relation {
        Sequence, Mapping, Scalar
    }
}
