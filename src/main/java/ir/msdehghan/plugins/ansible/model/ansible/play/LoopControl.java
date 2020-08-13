package ir.msdehghan.plugins.ansible.model.ansible.play;

import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

import static ir.msdehghan.plugins.ansible.model.yml.YamlTypes.*;

public class LoopControl extends YamlMappingType {
    public LoopControl() {
        super("task:loop_control", 5);

        addField("loop_var")
                .setType(STRING)
                .setDescription("Default: \"item\"");

        addField("index_var")
                .setType(STRING);

        addField("label")
                .setType(STRING);

        addField("pause")
                .setType(INTEGER) // TODO: float
                .setDescription("Default: 0");

        addField("extended")
                .setType(BOOLEAN);
    }
}
