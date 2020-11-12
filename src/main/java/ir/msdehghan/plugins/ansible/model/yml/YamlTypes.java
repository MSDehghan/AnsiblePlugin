package ir.msdehghan.plugins.ansible.model.yml;

import com.intellij.openapi.fileTypes.FileType;
import ir.msdehghan.plugins.ansible.model.yml.type.*;
import org.jetbrains.yaml.YAMLFileType;

public class YamlTypes {
    public static final YamlType ANY = new YamlAnyType();
    public static final YamlType STRING = new YamlStringType();
    public static final YamlType BOOLEAN = new YamlEnumType("Boolean").setOptions("true", "false");
    public static final YamlType INTEGER = new YamlIntegerType();
    public static final YamlType FLOAT = INTEGER; // TODO: replace with NUMBER
    public static final YamlType PATH = new YamlFilePathType("Path", true, null);
    public static final YamlType YAML_FILE_PATH = new YamlFilePathType("Path", false,
            new FileType[]{YAMLFileType.YML});

    private YamlTypes() {
        // This is a utility class.
    }
}
