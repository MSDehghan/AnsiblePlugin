# IntelliJ Ansible Plugin
[![Build](https://github.com/MSDehghan/AnsiblePlugin/workflows/Build/badge.svg)](https://github.com/MSDehghan/AnsiblePlugin/actions?query=workflow%3ABuild)
[![Version](https://img.shields.io/jetbrains/plugin/v/ir.msdehghan.plugins.ansible.svg)](https://plugins.jetbrains.com/plugin/14893-ansible)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/ir.msdehghan.plugins.ansible.svg)](https://plugins.jetbrains.com/plugin/14893-ansible)

Plugin that helps develop ansible files.

## Features
* Autocomplete playbooks fields
* Autocomplete role fields
* Autocomplete Ansible modules fields
* Access to all ansible docs locally
* Suggest role names

## Roadmap
* Ansible FQCN support
* Suggest defined variables
* Go to variable
* Go to play

## Contribution
To report bugs and feature requests you can create a Github issue. For code contribution you can create pull requests.

To regenerate the plugin's module documentation:

```sh
./gradlew generatePluginDocs
```

See [the extractor instructions](plugin-doc-extractor/README.md) for requirements and details.

Any type of contribution is **highly** appreciated.
