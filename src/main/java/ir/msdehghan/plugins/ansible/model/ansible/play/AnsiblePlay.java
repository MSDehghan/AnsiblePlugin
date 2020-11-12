package ir.msdehghan.plugins.ansible.model.ansible.play;

import ir.msdehghan.plugins.ansible.model.ansible.AnsibleFields;
import ir.msdehghan.plugins.ansible.model.ansible.role.Role;
import ir.msdehghan.plugins.ansible.model.ansible.task.AnsibleTask;
import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

import static ir.msdehghan.plugins.ansible.model.ansible.AnsibleFields.getCollectionsGroup;
import static ir.msdehghan.plugins.ansible.model.yml.type.api.YamlField.Relation.*;

public class AnsiblePlay extends YamlMappingType {
    public AnsiblePlay() {
        super("AnsiblePlay", 19 + AnsibleFields.getBaseGroup().size() +
                AnsibleFields.getTaggableGroup().size() + getCollectionsGroup().size());

        AnsibleFields.getBaseGroup().forEach(this::addField);
        AnsibleFields.getTaggableGroup().forEach(this::addField);
        getCollectionsGroup().forEach(this::addField);

        addField("fact_path").setType(YamlTypes.STRING)
                .setDescription("Set the fact path option for the fact gathering plugin controlled by gather_facts.");

        addField("force_handlers").setType(YamlTypes.STRING)
                .setDescription("Will force notified handler execution for hosts even if they failed during the play. Will not trigger if the play itself fails.");

        addField("gather_facts").setType(YamlTypes.BOOLEAN)
                .setDescription("A boolean that controls if the play will automatically run the 'setup' task to gather facts for the hosts.");

        addField("gather_subset")
                .setType(YamlTypes.STRING)
                .setType(SEQUENCE, YamlTypes.STRING)
                .setDescription("Allows you to pass subset options to the fact gathering plugin controlled by gather_facts.");

        addField("gather_timeout").setType(YamlTypes.INTEGER)
                .setDescription("Allows you to set the timeout for the fact gathering plugin controlled by gather_facts.");

        addField("handlers").setType(SEQUENCE, AnsibleTask.TYPE) // TODO: add handler type
                .setDescription("A section with tasks that are treated as handlers, these won't get executed normally, only when notified after each section of tasks is complete. A handler's listen field is not templatable.");

        addField("hosts").setRequired()
                .setDescription("A list of groups, hosts or host pattern that translates into a list of hosts that are the play's target.")
                .setType(YamlTypes.STRING)
                .setType(SEQUENCE, YamlTypes.STRING);

        addField("max_fail_percentage").setType(YamlTypes.STRING)
                .setDescription("can be used to abort the run after a given percentage of hosts in the current batch has failed.");

        addField("import_playbook").setType(YamlTypes.YAML_FILE_PATH)
                .setDescription("Includes a file with a list of plays to be executed");

        addField("order").setType(YamlTypes.STRING)
                .setDescription("Controls the sorting of hosts as they are used for executing the play. Possible values are inventory (default), sorted, reverse_sorted, reverse_inventory and shuffle.");

        addField("post_tasks").setType(SEQUENCE, AnsibleTask.TYPE)
                .setDescription("A list of tasks to execute after the tasks section.");

        addField("tasks").setType(SEQUENCE, AnsibleTask.TYPE)
                .setDescription("Main list of tasks to execute in the play, they run after roles and before post_tasks.");

        addField("pre_tasks").setType(SEQUENCE, AnsibleTask.TYPE)
                .setDescription("A list of tasks to execute before roles.");

        addField("roles").setType(SEQUENCE, Role.TYPE)
                .setDescription("List of roles to be imported into the play");

        addField("serial").setType(YamlTypes.INTEGER)
                .setDescription("Explicitly define how Ansible batches the execution of the current play on the play's target");

        addField("strategy").setType(YamlTypes.STRING)
                .setDescription("Allows you to choose the connection plugin to use for the play.");

        addField("vars_files")
                .setType(YamlTypes.YAML_FILE_PATH)
                .setType(SEQUENCE, YamlTypes.YAML_FILE_PATH)
                .setDescription("List of files that contain vars to include in the play.");

        addField("vars_prompt").setType(MAPPING, new VarsPrompt())
                .setDescription("list of variables to prompt for.");

        addField("user").setType(YamlTypes.STRING).setDeprecated()
                .setDescription("The use of 'user' is deprecated, and should be removed");
    }
}
