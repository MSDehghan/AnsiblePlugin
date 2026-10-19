# Generating module documentation

The extractor reads the JSON returned by `ansible-doc -t module --json <module names>`.
Module names are required. Discover installed module names with `ansible-doc -t module --list --json`
and pass the keys of that JSON object to the documentation command in a single invocation.
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

To install the generated files, copy the JSON files into `src/main/resources/docs/` and
`plugins_list.txt` into `src/main/resources/plugins_list.txt` in the root project. Regenerate
into a fresh directory when changing the set of input modules, to avoid retaining old JSON files.

For older Ansible input, check whether `copy.mode` needs its type changed to `raw`;
current Ansible documentation already uses `raw`.
