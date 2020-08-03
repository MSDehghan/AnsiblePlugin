package ir.msdehghan.plugins.ansible.model.ansible;

import ir.msdehghan.plugins.ansible.model.yml.YamlTypes;
import ir.msdehghan.plugins.ansible.model.yml.type.YamlMappingType;

public class AnsiblePlay extends YamlMappingType {
    public AnsiblePlay() {
        super("AnsiblePlay", 40);

        addField("any_errors_fatal").setType(YamlTypes.STRING)
                .setDescription("Force any un-handled task errors on any host to propagate to all hosts and end the play.");

        addField("become").setType(YamlTypes.BOOLEAN)
                .setDescription("Boolean that controls if privilege escalation is used or not on Task execution.");

        addField("become_exe").setType(YamlTypes.STRING);

        addField("become_flags").setType(YamlTypes.STRING)
                .setDescription("A string of flag(s) to pass to the privilege escalation program when become is True.");

        addField("become_method").setType(YamlTypes.STRING)
                .setDescription("Which method of privilege escalation to use (such as sudo or su).");

        addField("become_user").setType(YamlTypes.STRING)
                .setDescription("User that you 'become' after using privilege escalation. The remote/login user must have permissions to become this user.");

        addField("check_mode").setType(YamlTypes.BOOLEAN)
                .setDescription("A boolean that controls if a task is executed in 'check' mode");

        addField("connection").setType(YamlTypes.STRING)
                .setDescription("Allows you to change the connection plugin used for tasks to execute on the target.");

        addField("debugger").setType(YamlTypes.STRING)
                .setDescription("Enable debugging tasks based on state of the task result. See Playbook Debugger");

        addField("diff").setType(YamlTypes.STRING)
                .setDescription("Toggle to make tasks return 'diff' information or not.");

        addField("environment").setType(YamlTypes.ANY)
                .setDescription("A dictionary that gets converted into environment vars to be provided for the task upon execution. This cannot affect Ansible itself nor its configuration, it just sets the variables for the code responsible for executing the task.");

        addField("fact_path").setType(YamlTypes.STRING)
                .setDescription("Set the fact path option for the fact gathering plugin controlled by gather_facts.");

        addField("force_handlers").setType(YamlTypes.STRING)
                .setDescription("Will force notified handler execution for hosts even if they failed during the play. Will not trigger if the play itself fails.");

        addField("gather_facts").setType(YamlTypes.BOOLEAN)
                .setDescription("A boolean that controls if the play will automatically run the 'setup' task to gather facts for the hosts.");

        addField("gather_subset").setType(YamlTypes.STRING)
                .setDescription("Allows you to pass subset options to the fact gathering plugin controlled by gather_facts.");

        addField("gather_timeout").setType(YamlTypes.STRING)
                .setDescription("Allows you to set the timeout for the fact gathering plugin controlled by gather_facts.");

        addField("handlers").setType(YamlTypes.STRING)
                .setDescription("A section with tasks that are treated as handlers, these won't get executed normally, only when notified after each section of tasks is complete. A handler's listen field is not templatable.");

        addField("hosts").setType(YamlTypes.STRING).setRequired()
                .setDescription("A list of groups, hosts or host pattern that translates into a list of hosts that are the play's target.");

        addField("ignore_errors").setType(YamlTypes.BOOLEAN)
                .setDescription("Boolean that allows you to ignore task failures and continue with play. It does not affect connection errors.");

        addField("ignore_unreachable").setType(YamlTypes.BOOLEAN)
                .setDescription("Boolean that allows you to ignore unreachable hosts and continue with play. This does not affect other task errors (see ignore_errors) but is useful for groups of volatile/ephemeral hosts.");

        addField("max_fail_percentage").setType(YamlTypes.STRING)
                .setDescription("can be used to abort the run after a given percentage of hosts in the current batch has failed.");

        addField("module_defaults").setType(YamlTypes.STRING)
                .setDescription("Specifies default parameter values for modules.");

        addField("name").setType(YamlTypes.STRING)
                .setDescription("Identifier. Can be used for documentation, in or tasks/handlers.");

        addField("no_log").setType(YamlTypes.BOOLEAN)
                .setDescription("Boolean that controls information disclosure.");

        addField("order").setType(YamlTypes.STRING)
                .setDescription("Controls the sorting of hosts as they are used for executing the play. Possible values are inventory (default), sorted, reverse_sorted, reverse_inventory and shuffle.");

        addField("port").setType(YamlTypes.INTEGER)
                .setDescription("Used to override the default port used in a connection.");

        addField("post_tasks").setType(YamlTypes.ANY)
                .setDescription("A list of tasks to execute after the tasks section.");

        addField("pre_tasks").setType(YamlTypes.ANY)
                .setDescription("A list of tasks to execute before roles.");

        addField("remote_user").setType(YamlTypes.STRING)
                .setDescription("User used to log into the target via the connection plugin.");

        addField("roles").setType(YamlTypes.ANY)
                .setDescription("List of roles to be imported into the play");

        addField("run_once").setType(YamlTypes.BOOLEAN)
                .setDescription("Boolean that will bypass the host loop, forcing the task to attempt to execute on the first host available and afterwards apply any results and facts to all active hosts in the same batch.");

        addField("serial").setType(YamlTypes.INTEGER)
                .setDescription("Explicitly define how Ansible batches the execution of the current play on the play's target");

        addField("strategy").setType(YamlTypes.STRING)
                .setDescription("Allows you to choose the connection plugin to use for the play.");

        addField("tags").setType(YamlTypes.ANY)
                .setDescription("Tags applied to the task or included tasks, this allows selecting subsets of tasks from the command line.");

        addField("tasks").setType(YamlTypes.ANY)
                .setDescription("Main list of tasks to execute in the play, they run after roles and before post_tasks.");

        addField("throttle").setType(YamlTypes.INTEGER)
                .setDescription("Limit number of concurrent task runs on task, block and playbook level. This is independent of the forks and serial settings, but cannot be set higher than those limits. For example, if forks is set to 10 and the throttle is set to 15, at most 10 hosts will be operated on in parallel.");

        addField("vars").setType(YamlTypes.ANY)
                .setDescription("Dictionary/map of variables.");

        addField("vars_files").setType(YamlTypes.ANY)
                .setDescription("List of files that contain vars to include in the play.");

        addField("vars_prompt").setType(YamlTypes.ANY)
                .setDescription("list of variables to prompt for.");

        addField("user").setType(YamlTypes.STRING).setDeprecated()
                .setDescription("The use of 'user' is deprecated, and should be removed");
    }
}
