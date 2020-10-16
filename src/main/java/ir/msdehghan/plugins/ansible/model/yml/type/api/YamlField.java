package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.codeInsight.lookup.LookupElement;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlType;

import static java.lang.Character.toUpperCase;

public interface YamlField {
    LookupElement getLookupElement();

    String getName();

    YamlType getType(Relation relation);

    String generateDoc();

    enum Relation {
        SEQUENCE, MAPPING, SCALAR;


        @Override
        public String toString() {
            return toUpperCase(name().charAt(0)) + name().substring(1).toLowerCase();
        }
    }
}
