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
    private static final int FIELDS_SIZE = 18;

    private AnsibleTask() {
        super("Task", FIELDS_SIZE + getBaseGroup().size() + getConditionalGroup().size()
                + getTaggableGroup().size() +getDelegateGroup().size() + getCollectionsGroup().size());

        getBaseGroup().forEach(this::addField);
        getConditionalGroup().forEach(this::addField);
        getTaggableGroup().forEach(this::addField);
        getDelegateGroup().forEach(this::addField);
        getCollectionsGroup().forEach(this::addField);

        addField("action")
                .setType(YamlTypes.STRING)
                .setDescription("The ‘action’ to execute for a task, it normally translates into a module or action plugin.");

        addField("args")
                .setType(MAPPING, YamlTypes.ANY)
                .setDescription("A secondary way to add arguments into a task. Takes a dictionary in which keys map to options and values.");


        addField("async")
                .setType(YamlTypes.INTEGER)
                .setDescription("Run a task asynchronously if the action supports this; value is maximum runtime in seconds. Default: 0");

        addField("changed_when")
                .setType(YamlTypes.BOOLEAN)
                .setType(SEQUENCE, YamlTypes.BOOLEAN)
                .setDescription("Conditional expression that overrides the task’s normal ‘changed’ status.");

        addField("delay")
                .setType(YamlTypes.INTEGER)
                .setDescription("Number of seconds to delay between retries. This setting is only used in combination with until. Default: 5");

        addField("failed_when")
                .setType(YamlTypes.BOOLEAN)
                .setType(SEQUENCE, YamlTypes.BOOLEAN)
                .setDescription("Conditional expression that overrides the task’s normal ‘failed’ status.");

        addField("local_action")
                .setType(YamlTypes.STRING)
                .setDescription("Same as 'action' but also implies 'delegate_to: localhost'");

        addField("loop")
                .setType(SEQUENCE, YamlTypes.ANY)
                .setDescription("Takes a list for the task to iterate over, saving each list element into the 'item' variable (configurable via loop_control)");

        addField("loop_control")
                .setType(MAPPING, new LoopControl())
                .setDescription("Several keys here allow you to modify/set loop behaviour in a task.");

        addField("notify")
                .setType(YamlTypes.STRING)
                .setType(SEQUENCE, YamlTypes.STRING)
                .setDescription("List of handlers to notify when the task returns a ‘changed=True’ status.");

        addField("poll")
                .setType(YamlTypes.INTEGER)
                .setDescription("Sets the polling interval in seconds for async tasks (default 10s).");

        addField("register")
                .setType(YamlTypes.STRING)
                .setDescription("Name of variable that will contain task status and module return data.");

        addField("retries")
                .setType(YamlTypes.INTEGER)
                .setDescription("Number of retries before giving up in a 'until' loop. This setting is only used in combination with 'until'. Default: 3");

        addField("until")
                .setType(YamlTypes.STRING)
                .setType(SEQUENCE, YamlTypes.STRING)
                .setDescription("This keyword implies a ‘retries loop’ that will go on until the condition supplied here is met or we hit the retries limit.");

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
