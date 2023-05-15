# Pedestal • Librairies for performant multiplatform apps

OpenSavvy Pedestal is a collection of open source utilities for unified software architecture, aiming to solve common problems in a simple and elegant way with Kotlin Multiplatform.
In particular, Pedestal focuses on fullstack development.

The rendered documentation for the latest released version is [available here](https://opensavvy.gitlab.io/pedestal/documentation/).
The release notes and changelogs are [available here](https://gitlab.com/opensavvy/pedestal/-/releases) or as Git tags.

This project contains the following modules:

- [Pedestal Logger](logger/README.md) is a simple logger implementation for multiplatform projects.
- [Pedestal Progress](progress/README.md) models the progress of an asynchronous task and its subtasks (companion: [KotlinX.Coroutines](progress-coroutines)).
- [Pedestal State](state/README.md) models the success or failure of an operation, without losing progress information (companions: [KotlinX.Coroutines](state-coroutines), [Arrow](state-arrow)).
- [Pedestal Cache](cache/README.md) is a collection of reactive cache implementations to aggressively reduce bandwidth usage with easy integration with reactive UI frameworks, like React or Compose.
- [Pedestal Backbone](backbone/README.md) is an opinionated architectural pattern, using aggressive caching in all layers of a multiplatform application to entirely abstract away mutability behind coroutines.

Pedestal focuses heavily on Kotlin and attempts to depend on as few external dependencies as possible.
Where possible, compatibility modules are provided to interoperate with other ecosystems.

## In the wild

OpenSavvy Pedestal is used by these projects:

- [Formulaide](https://gitlab.com/opensavvy/formulaide) is a web form editor with integrated workflow management,
- [Decouple](https://gitlab.com/opensavvy/decouple) is a Kotlin Multiplatform UI framework aiming to decouple design systems from UI logic.

## Using in your own projects

You can easily add any module using Gradle:

```kotlin
// First, add the OpenSavvy repository
repositories {
	maven {
		name = "OpenSavvy Pedestal"
		url = uri("https://gitlab.com/api/v4/projects/37325377/packages/maven")
	}
}

// You can now add a dependency on the various modules:
dependencies {
    implementation("opensavvy.pedestal:backbone:<the version you want>")
}
```

Currently, we only publish builds for Kotlin/JVM, Kotlin/JS (IR only) and Kotlin/Native for iOS.
This project has very little platform-specific code, and would be easy to port to any other platform—we just don't have the need for it.
If you are interested in another platform, we encourage contributions that add the relevant CI configuration to test and deploy for that platform.

- [Release list](https://gitlab.com/opensavvy/pedestal/-/releases)
- [Artifact list](https://gitlab.com/opensavvy/pedestal/-/packages)
- [Breaking changes migration guide](docs/MIGRATION_GUIDE.md)

## Development

Pedestal is managed by Gradle, which requires a valid Java installation.
On Windows, substitute `./gradlew` by `gradlew.bat` in the given commands.

Gradle is invoked by running `./gradlew <tasks here>` in the project root.

- `./gradlew projects` to list the available projects,
- `./gradlew tasks` to list the available tasks in the root project,
- `./gradlew <project>:<task>` to execute a task from another project (for example, `./gradlew backbone:tasks` to get
  the list of tasks in the Backbone project).

## Contribution

To learn how to contribute to the
project, [please read our wiki](https://gitlab.com/opensavvy/wiki/-/blob/main/README.md).
