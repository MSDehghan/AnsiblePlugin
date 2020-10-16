package ir.msdehghan.plugins.ansible.model.yml.type.api;

import com.intellij.codeInsight.lookup.LookupElement;
import java.util.List;

public interface ValueProvider {
    List<LookupElement> getValueLookups();
}
