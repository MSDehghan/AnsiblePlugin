package ir.msdehghan.plugins.ansible.model.ansible.task;

import ir.msdehghan.plugins.ansible.model.ansible.play.LoopControl;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static ir.msdehghan.plugins.ansible.model.ansible.AnsibleFields.*;
import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.MAPPING;
import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.SEQUENCE;

public class AnsibleTask extends YamlMappingType {
    public static final AnsibleTask TYPE = new AnsibleTask();
    private static final int FIELDS_SIZE = 20;

    private AnsibleTask() {
        super("Task", FIELDS_SIZE + BASE.size() + CONDITIONAL.size() + TAGGABLE.size());

        BASE.forEach(this::addField);
        CONDITIONAL.forEach(this::addField);
        TAGGABLE.forEach(this::addField);

        addField("args")
                .setType(MAPPING, YamlTypes.ANY)
                .setDescription("A secondary way to add arguments into a task. Takes a dictionary in which keys map to options and values.");

        addField("action")
                .setType(YamlTypes.STRING)
                .setDescription("");

        addField("async")
                .setType(YamlTypes.INTEGER)
                .setDescription("Default: 0");

        addField("changed_when")
                .setType(YamlTypes.BOOLEAN)
                .setType(SEQUENCE, YamlTypes.BOOLEAN)
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
                .setType(SEQUENCE, YamlTypes.BOOLEAN)
                .setDescription("");

        addField("loop")
                .setType(SEQUENCE, YamlTypes.ANY)
                .setDescription("");

        addField("loop_control")
                .setType(MAPPING, new LoopControl())
                .setDescription("");

        addField("notify")
                .setType(YamlTypes.STRING)
                .setType(SEQUENCE, YamlTypes.STRING)
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

        addField("block")
                .setType(SEQUENCE, this)
                .setDescription("Blocks create logical groups of tasks. Blocks also offer ways to handle task errors," +
                        " similar to exception handling in many programming languages. See 'rescue' and 'always' fields.");

        addField("rescue")
                .setType(SEQUENCE, this)
                .setDescription("Rescue blocks specify tasks to run when an earlier task in a block fails. " +
                        "This approach is similar to exception handling in many programming languages. " +
                        "Ansible only runs rescue blocks after a task returns a ‘failed’ state. " +
                        "Bad task definitions and unreachable hosts will not trigger the rescue block.");

        addField("always")
                .setType(SEQUENCE, this)
                .setDescription("Tasks in the always section run no matter what the task status of the previous block is.");

        loadPluginNames();
    }

    private void loadPluginNames() {
        try (InputStream pluginsInputStream = AnsibleTask.class.getResourceAsStream("/plugins_list.txt")) {
            BufferedReader pluginsReader = new BufferedReader(new InputStreamReader(pluginsInputStream));
            String name;
            while ((name = pluginsReader.readLine()) != null) {
                addField(new AnsibleModuleField(name));
            }
        } catch (IOException e) {
            throw new IllegalStateException("Can't read ansible module names.", e);
        }
    }
}
