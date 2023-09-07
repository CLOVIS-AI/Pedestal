# OpenSavvy Playground: Gradle

> [Repository](https://gitlab.com/opensavvy/playgrounds/gradle)

The Gradle playground contains a Hello World idiomatic Gradle project with maintainability and performance in mind.

[TOC]

## The Gradle Wrapper

The Gradle Wrapper (`gradlew`) is a small binary that downloads a specific version of Gradle.
It is configured in the [gradle](../../gradle/wrapper) directory, and started by the [gradlew](../../gradlew) and [gradlew.bat](../../gradlew.bat) scripts.

Using the Wrapper ensures all team members and CI use the exact same version of Gradle.
Because Gradle is responsible for downloading all other project dependencies, the only dependency for using the project is having a half-modern JVM.

To learn more about the Gradle Wrapper, see [its documentation](https://docs.gradle.org/current/userguide/gradle_wrapper.html).

## Convention plugins

A Gradle build is structured as follows:
- A `settings.gradle.kts` file for the entire build, which configures the project structure,
- A `build.gradle.kts` file for each project, which configures the different tasks that exist.

However, as a repository grows, we want to ensure all projects are built similarly. In practice, this means we end up with a lot of duplication, which makes maintaining the build harder.

In the past, the recommended solution was to create a `buildSrc` folder, which is a special project that is built before the configuration phase starts. Everything from the `buildSrc` folder is injected into `build.gradle.kts` files, which means we can declare variables or functions and use them everywhere. However, `buildSrc` is slow, and each change requires a recompilation of all `build.gradle.kts` files as well as complete cache destruction, making subsequent builds much slower.

Some projects also used the `allprojects` and `subprojects` blocks in the root project to share configuration. This breaks the project isolation rules (a project should not impact the configuration of another project), which makes [configuration-on-demand impossible](https://docs.gradle.org/current/userguide/multi_project_configuration_and_execution.html#sec:configuration_on_demand), and makes understanding complicated projects harder.

Instead, we use convention plugins. Convention plugins are small plugins, written in the same syntax as `build.gradle.kts` files. They often use other plugins, and configure them with the conventions followed by the rest of the repository. Ideally, this means all configuration is moved to configuration plugins, which are then reused as many times as needed, avoiding any configuration in regular projects.

Multiple people put convention plugins in multiple places, for example in a `build-logic` folder. We prefer to put them in [gradle/conventions](../../gradle/conventions).

To learn more about convention plugins, see [their documentation](https://docs.gradle.org/current/userguide/custom_plugins.html#sec:precompiled_plugins).

## Version catalog

We centralize the version numbers of the dependencies we use in the [libs.versions.toml](../../gradle/libs.versions.toml) file.

To learn more about the version catalog, see [its documentation](https://docs.gradle.org/current/userguide/platforms.html).

## Configuration cache

The configuration cache allows Gradle to locally store the results of the configuration phase for a specific execution.
If we later call Gradle with the same execution (same environment variables, same requested tasks…), Gradle reuses the stored entry and entirely skips the configuration phase.

To learn more about the configuration cache, see [its documentation](https://docs.gradle.org/current/userguide/configuration_cache.html).

## Build cache

The build cache allows Gradle to store locally, and optionally remotely, the result of specific tasks. When the same tasks are executed again, Gradle gets the results of the task from the cache instead of executing it.

OpenSavvy has its own Gradle Build Cache remote instance, which is publicly readable, allowing anyone who clones a project to use it. Only our CI jobs on protected branches and tags is allowed to push to the cache, ensuring it only stores results we trust.

To learn more about the build cache, see [its documentation](https://docs.gradle.org/current/userguide/build_cache.html).

## Configuration on demand

By default, Gradle configures all projects before starting executing tasks. With configuration on demand enabled, Gradle only configures projects which take part in the requested tasks.

To learn more about configuration on demand, see [its documentation](https://docs.gradle.org/current/userguide/multi_project_configuration_and_execution.html#sec:configuration_on_demand).
