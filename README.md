# Pedestal • Librairies for performant multiplatform apps

OpenSavvy Pedestal is a collection of open source utilities for unified software architecture, aiming to solve common problems in a simple and elegant way with Kotlin Multiplatform.
In particular, Pedestal focuses on fullstack development.

The rendered documentation for the latest released version is [available here](https://opensavvy.gitlab.io/pedestal/api-docs/).
The release notes and changelogs are [available here](https://gitlab.com/opensavvy/pedestal/-/releases) or as Git tags.

This project contains the following modules:

- [Pedestal Progress](https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/progress/index.html) models the progress of an asynchronous task and its subtasks.
- [Pedestal State](https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/state/index.html) models the success or failure of an operation, without losing progress information.
- [Pedestal Cache](https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/cache/index.html) is a collection of reactive cache implementations to aggressively reduce bandwidth usage with easy integration with reactive UI frameworks, like React or Compose.
- [Pedestal Backbone](https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/backbone/index.html) is an opinionated architectural pattern, using aggressive caching in all layers of a multiplatform application to entirely abstract away mutability behind coroutines.

Pedestal focuses heavily on Kotlin and attempts to depend on as few external dependencies as possible.
Where possible, compatibility modules are provided to interoperate with other ecosystems.

## In the wild

OpenSavvy Pedestal is used by these projects:

- [Formulaide](https://gitlab.com/opensavvy/formulaide) is a web form editor with integrated workflow management,
- [Decouple](https://gitlab.com/opensavvy/decouple) is a Kotlin Multiplatform UI framework aiming to decouple design systems from UI logic.

## Using in your own projects

You can easily add any module using Gradle:

```kotlin
repositories {
	// Pedestal is available on Maven Central
	mavenCentral()

	// Or, if you prefer, Pedestal is also available in our own maven repository
	maven {
		name = "OpenSavvy Pedestal"
		url = uri("https://gitlab.com/api/v4/projects/37325377/packages/maven")
	}
}

// You can now add a dependency on the various modules:
dependencies {
	implementation("dev.opensavvy.pedestal:backbone:<the version you want>")
}
```

Currently, we only publish builds for Kotlin/JVM, Kotlin/JS (IR only) and Kotlin/Native for iOS.
This project has very little platform-specific code, and would be easy to port to any other platform—we just don't have the need for it.
If you are interested in another platform, we encourage contributions that add the relevant CI configuration to test and deploy for that platform.

- [Release list](https://gitlab.com/opensavvy/groundwork/pedestal/-/releases)
- [Artifact list for MavenCentral](https://search.maven.org/search?q=g:dev.opensavvy.pedestal)
- [Artifact list for the GitLab Repository](https://gitlab.com/opensavvy/groundwork/pedestal/-/packages)
- [Breaking changes migration guide](docs/MIGRATION_GUIDE.md)

## Development

Pedestal is managed by Gradle, which requires a valid Java installation.
On Windows, substitute `./gradlew` by `gradlew.bat` in the following commands.

Gradle is invoked by running `./gradlew <tasks here>` in the project root.

- `./gradlew projects` to list the available projects,
- `./gradlew tasks` to list the available tasks in the root project,
- `./gradlew <project>:<task>` to execute a task from another project (for example, `./gradlew backbone:tasks` to get
  the list of tasks in the Backbone project).

We recommend using IntelliJ IDEA (Community or Ultimate), for which we provide the configuration (run configurations, coding style…).

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
- To learn more about our coding conventions and workflow, see the [OpenSavvy website](https://opensavvy.dev/open-source/index.html).
- This project is based on the [OpenSavvy Playground](docs/playground/README.md), a collection of preconfigured project templates.

If you don't want to clone this project on your machine, it is also available using [GitPod](https://www.gitpod.io/) and [DevContainer](https://containers.dev/) ([VS Code](https://code.visualstudio.com/docs/devcontainers/containers) • [IntelliJ & JetBrains IDEs](https://www.jetbrains.com/help/idea/connect-to-devcontainer.html)). Don't hesitate to create issues if you have problems getting the project up and running.
