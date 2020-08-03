package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.codeInsight.lookup.LookupElementBuilder;

import java.util.List;

public interface ValueProvider {
    List<? extends LookupElementBuilder> getValueLookups();
}
