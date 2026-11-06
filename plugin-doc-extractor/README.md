# Generating module documentation

With Python 3 and Ansible installed and `python3` and `ansible-doc` on `PATH`, run
from the repository root:

```sh
./gradlew generatePluginDocs
```

This collects the documentation of every installed module and collection, then replaces
`src/main/resources/docs/` and `src/main/resources/plugins_list.txt` in the root project.
Activate the desired Ansible environment first.

## Generating from a specific input

The extractor reads the JSON returned by `ansible-doc -t module --json <module names>`.
Module names are required. Discover installed module names with `ansible-doc -t module --list_files`
and pass the names in its first column to the documentation command.
Only installed modules and collections are included.

For example, from the repository root:

```sh
ansible-doc -t module --json ansible.builtin.copy community.general.composer \
  > plugin-doc-extractor/src/main/resources/plugins.json
./gradlew :plugin-doc-extractor:run
```

The default input is the subproject's `src/main/resources/plugins.json`. Output goes to
`plugin-doc-extractor/docs/`. To use other paths, run:

```sh
./gradlew :plugin-doc-extractor:run --args="--input /path/plugins.json --output /path/docs"
```

The output contains one JSON file per name and a sorted `plugins_list.txt`. By default,
both qualified names and short-name aliases are emitted. Built-ins take priority for
short-name collisions; other collisions are resolved in alphabetical order. Every qualified
name, including redirected input names, remains available. Add `--fqcn` to emit only
qualified names.

`fields` contains module options. Each option's `suboptions` recursively retains nested
options, including options within lists of dictionaries. Modules without options omit `fields`.
`import_playbook` and the pseudo-option `free-form` are excluded automatically.

`returns` retains complete return-value definitions; `returnFields` retains their names for
the existing documentation reader. `examples` and `metadata` retain their source values.
Module identity is stored in `qualifiedName`, `collection`, and `filename`. Additional module,
option, and deprecation metadata is retained in `documentation`. Map-shaped choices retain
their descriptions in `choiceDescriptions` alongside the normalized `choices` list.

To install the output manually, copy the JSON files into `src/main/resources/docs/` and
`plugins_list.txt` into `src/main/resources/`, or use `./gradlew generatePluginDocs`.
