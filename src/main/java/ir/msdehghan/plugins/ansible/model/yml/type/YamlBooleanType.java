package ir.msdehghan.plugins.ansible.model.yml.type;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;

import java.util.Arrays;
import java.util.List;


public class YamlBooleanType extends YamlType implements ValueProvider {
    public YamlBooleanType() {
        super("Boolean");
    }

    @Override
    public List<? extends LookupElementBuilder> getValueLookups() {
        return Arrays.asList(LookupElementBuilder.create("true"), LookupElementBuilder.create("false"));
    }
}
