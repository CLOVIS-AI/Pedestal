# OpenSavvy Playground: Baseline

> [Repository](https://gitlab.com/opensavvy/playgrounds/baseline)

The Baseline playground contains common configuration we use in all our projects.

[TOC]

## JetBrains IDEs (IntelliJ, WebStorm…)

OpenSavvy recommends using IntelliJ-based IDEs from JetBrains.
The Playground contains configuration for multiple aspects of the project. 

### Code style

The Playground contains the configuration to follow the [OpenSavvy coding style](https://gitlab.com/opensavvy/wiki/-/blob/main/coding-style/README.md).

The Playground also has the [Git Toolbox](https://plugins.jetbrains.com/plugin/7499-gittoolbox) configuration to check that commits confirm to our commit style.

### Copyright

The Playground contains the license text for the licenses [approved by OpenSavvy](https://gitlab.com/opensavvy/wiki/-/blob/main/licensing/README.md).

To use a license in your project, go to `File | Settings | Editor | Copyright` and change the default project copyright, or create various scopes to use different licenses in different areas of the project. 

## GitLab

### CI/CD

The Playground has configuration for GitLab CI for the basic workflow.

## Remote development and build environment

Usually, the build process is created to cater to the "developer" archetype, who has the project installed on their workstation and regularly submits changes. However, in the real world, many users of the repository do not fit this archetype:
- artists, product owners and other non-technical roles may want to edit some files,
- a regular developer may want to make some changes from a secondary device,
- a developer from another project may want to make some quick changes (especially common for Open Source projects).

In all these cases, going through the entire repository setup process is a waste of time when compared to the contribution.
This often feels discouraging for contributors, especially when they try anyway and get stuck on version conflicts of some system dependency.

To avoid this issue, the Playground is compatible with multiple ways of opening the project without having to go through the project configuration.

### Gitpod

[Gitpod](https://gitpod.io/) is a cloud-based development platform. It allows to open a branch of the repository and execute it directly from the browser. It is especially convenient for projects with a web-based frontend, or projects with no UI (e.g. libraries). Gitpod cannot run native GUI apps.

Gitpod is configured in the [.gitpod.yaml](../../.gitpod.yml) file.

Useful links:
- [Link your GitLab account](https://docs.gitlab.com/ee/integration/gitpod.html)
- [Develop using JetBrains IDEs](https://www.gitpod.io/docs/integrations/jetbrains-gateway)
- [SSH access](https://www.gitpod.io/docs/references/ides-and-editors/command-line)

### Dev containers

[Dev containers](https://containers.dev/) is a standard for containerizing the development environment, created by Microsoft. [GitHub Codespaces](https://github.com/features/codespaces) uses them to run a project in the cloud, and IDEs can use them to locally start a containerized development environment, so you don't have to install anything.

Because it is possible to start projects locally in a container, and not just in the cloud, dev containers can be used as a daily driver on a project to entirely avoid configuring the environment. The specification is also more complete, allowing to start services using docker-compose, etc (though support by IDE varies).

Dev containers are configured in the [.devcontainer](../../.devcontainer) directory.

Useful links:
- [Develop using VS Code](https://code.visualstudio.com/docs/devcontainers/containers)
- [Develop using JetBrains IDEs](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)
- [Open in the cloud with GitHub Codespace](https://github.com/features/codespaces)
- [Open in the cloud with JetBrains Space](https://www.jetbrains.com/help/space/develop-in-a-dev-environment.html)
