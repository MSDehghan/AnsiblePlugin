package ir.msdehghan.plugins.ansible.model.yml;

import ir.msdehghan.plugins.ansible.model.yml.type.*;

public class YamlTypes {
    public static final YamlType ANY = new YamlAnyType();
    public static final YamlType STRING = new YamlStringType();
    public static final YamlType BOOLEAN = new YamlEnumType("Boolean").setOptions("true", "false");
    public static final YamlType INTEGER = new YamlIntegerType();

    private YamlTypes() {
        // This is a utility class.
    }
}
