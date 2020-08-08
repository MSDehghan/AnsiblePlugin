package ir.msdehghan.plugins.ansible.model.ansible.play;

import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

import static ir.msdehghan.plugins.ansible.model.yml.YamlTypes.BOOLEAN;
import static ir.msdehghan.plugins.ansible.model.yml.YamlTypes.STRING;

public class VarsPrompt extends YamlMappingType {
    public VarsPrompt() {
        super("play:vars_prompt", 7);

        addField("name")
                .setType(STRING)
                .setRequired();

        addField("prompt")
                .setType(STRING);

        addField("default")
                .setType(STRING);

        addField("private")
                .setType(BOOLEAN);

        addField("confirm")
                .setType(BOOLEAN);

        addField("encrypt")
                .setType(STRING);

        addField("salt_size")
                .setType(STRING);

        addField("salt")
                .setType(STRING);

        addField("unsafe")
                .setType(STRING);
    }
}
