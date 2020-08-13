package ir.msdehghan.plugins.ansible.model.ansible.task;

import ir.msdehghan.plugins.ansible.model.ansible.AnsibleFields;
import ir.msdehghan.plugins.ansible.model.ansible.play.LoopControl;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.Mapping;
import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.Sequence;

public class AnsibleTask extends YamlMappingType {
    public static final AnsibleTask TYPE = new AnsibleTask();

    private AnsibleTask() {
        super("play:task", 10);

        AnsibleFields.BASE.forEach(this::addField);
        AnsibleFields.CONDITIONAL.forEach(this::addField);
        AnsibleFields.TAGGABLE.forEach(this::addField);

        addField("args")
                .setType(Mapping, YamlTypes.ANY, true)
                .setDescription("A secondary way to add arguments into a task. Takes a dictionary in which keys map to options and values.");

        addField("action")
                .setType(YamlTypes.STRING)
                .setDescription("");

        addField("async")
                .setType(YamlTypes.INTEGER)
                .setDescription("Default: 0");

        addField("changed_when")
                .setType(YamlTypes.BOOLEAN)
                .setType(Sequence, YamlTypes.BOOLEAN)
                .setDescription("");

        addField("delay")
                .setType(YamlTypes.INTEGER)
                .setDescription("Default: 5");

        addField("delegate_to")
                .setType(YamlTypes.STRING)
                .setDescription("");

        addField("delegate_facts")
                .setType(YamlTypes.BOOLEAN)
                .setDescription("");

        addField("failed_when")
                .setType(YamlTypes.BOOLEAN)
                .setType(Sequence, YamlTypes.BOOLEAN)
                .setDescription("");

        addField("loop")
                .setType(Sequence, YamlTypes.ANY, true)
                .setDescription("");

        addField("loop_control")
                .setType(Mapping, new LoopControl(), true)
                .setDescription("");

        addField("notify")
                .setType(YamlTypes.STRING)
                .setType(Sequence, YamlTypes.STRING)
                .setDescription("");

        addField("poll")
                .setType(YamlTypes.INTEGER)
                .setDescription("");

        addField("register")
                .setType(YamlTypes.STRING)
                .setDescription("");

        addField("retries")
                .setType(YamlTypes.INTEGER)
                .setDescription("Default: 3");

        loadPluginNames();
    }

    private void loadPluginNames() {
        try {

            InputStream pluginsInputStream = AnsibleTask.class.getResourceAsStream("/plugins_list.txt");
            BufferedReader pluginsReader = new BufferedReader(new InputStreamReader(pluginsInputStream));
            String name;
            while ((name = pluginsReader.readLine()) != null) {
                addField(name).setType(YamlTypes.ANY);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't read ansible module names.", e);
        }
    }
}
