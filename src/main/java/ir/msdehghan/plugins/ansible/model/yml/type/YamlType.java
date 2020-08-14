package ir.msdehghan.plugins.ansible.model.yml.type;

public abstract class YamlType {
    protected final String name;

    public YamlType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "YamlType:" + name;
    }
}
