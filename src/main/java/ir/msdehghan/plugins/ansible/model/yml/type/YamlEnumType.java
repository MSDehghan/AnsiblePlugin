package ir.msdehghan.plugins.ansible.model.yml.type;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.util.ArrayUtil;
import ir.msdehghan.plugins.ansible.model.yml.type.api.ValueProvider;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class YamlEnumType extends YamlType implements ValueProvider {
    private String[] options = ArrayUtil.EMPTY_STRING_ARRAY;

    public YamlEnumType(String name)
    {
        super(name);
    }

    public YamlEnumType()
    {
        super("Enum");
    }

    public YamlEnumType setOptions(String... options) {
        this.options = options;
        return this;
    }

    public static YamlEnumType of(String... options) {
        return new YamlEnumType().setOptions(options);
    }

    @Override
    public List<? extends LookupElementBuilder> getValueLookups() {
        return Arrays.stream(options).map(LookupElementBuilder::create).collect(Collectors.toList());
    }
}
