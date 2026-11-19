# IntelliJ Ansible Plugin
[![Build](https://github.com/MSDehghan/AnsiblePlugin/workflows/Build/badge.svg)](https://github.com/MSDehghan/AnsiblePlugin/actions?query=workflow%3ABuild)
[![Version](https://img.shields.io/jetbrains/plugin/v/ir.msdehghan.plugins.ansible.svg)](https://plugins.jetbrains.com/plugin/14893-ansible)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/ir.msdehghan.plugins.ansible.svg)](https://plugins.jetbrains.com/plugin/14893-ansible)

An IntelliJ Platform plugin for writing Ansible playbooks and roles.

## Installation

Install [Ansible](https://plugins.jetbrains.com/plugin/14893-ansible) from the JetBrains Marketplace in your IDE. Plugin 2.0.0 requires an IntelliJ Platform 2026.2 or newer IDE with YAML and JSON support.

## Features

* Completion for playbook fields, role fields, and role names.
* Completion and Quick Documentation for bundled Ansible modules and their options, using short or fully qualified module names (for example, `copy` or `ansible.builtin.copy`).
* Offline module documentation for the Ansible collections bundled with the plugin.

## Roadmap
* Suggest defined variables
* Go to variable
* Go to play

## Building from source

Use JDK 25, Python 3.14, and Ansible 15.0.0 to match CI. Generate the bundled module docs before packaging the plugin:

```sh
python3 -m venv .venv
source .venv/bin/activate
python -m pip install ansible==15.0.0
./gradlew generatePluginDocs buildPlugin
```

The plugin ZIP is written to `build/distributions/`. See [the extractor instructions](plugin-doc-extractor/README.md) for details about using a different Ansible environment.

## Contributing

Report bugs or request features in [GitHub Issues](https://github.com/MSDehghan/AnsiblePlugin/issues). Pull requests are welcome.
