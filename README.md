# Pedestal • Librairies for performant multiplatform apps

OpenSavvy Pedestal is a collection of open source utilities for unified software architecture, aiming to solve common problems in a simple and elegant way with Kotlin Multiplatform.
In particular, Pedestal focuses on fullstack development.

The rendered documentation for the latest released version is [available here](https://opensavvy.gitlab.io/pedestal/documentation/).
The release notes and changelogs are [available here](https://gitlab.com/opensavvy/pedestal/-/releases) or as Git tags.

This project contains the following modules:

- [Pedestal Logger](https://opensavvy.gitlab.io/pedestal/documentation/logger/index.html) is a simple logger implementation for multiplatform projects.
- [Pedestal Progress](https://opensavvy.gitlab.io/pedestal/documentation/progress/index.html) models the progress of an asynchronous task and its subtasks (companion: [KotlinX.Coroutines](https://opensavvy.gitlab.io/pedestal/documentation/progress-coroutines/index.html)).
- [Pedestal State](https://opensavvy.gitlab.io/pedestal/documentation/state/index.html) models the success or failure of an operation, without losing progress information (companions: [KotlinX.Coroutines](https://opensavvy.gitlab.io/pedestal/documentation/state-coroutines/index.html), [Arrow](https://opensavvy.gitlab.io/pedestal/documentation/state-arrow/index.html)).
- [Pedestal Cache](https://opensavvy.gitlab.io/pedestal/documentation/cache/index.html) is a collection of reactive cache implementations to aggressively reduce bandwidth usage with easy integration with reactive UI frameworks, like React or Compose.
- [Pedestal Backbone](https://opensavvy.gitlab.io/pedestal/documentation/backbone/index.html) is an opinionated architectural pattern, using aggressive caching in all layers of a multiplatform application to entirely abstract away mutability behind coroutines.

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

- [Release list](https://gitlab.com/opensavvy/pedestal/-/releases)
- [Artifact list for MavenCentral](https://search.maven.org/search?q=g:dev.opensavvy.pedestal)
- [Artifact list for the GitLab Repository](https://gitlab.com/opensavvy/pedestal/-/packages)
- [Breaking changes migration guide](docs/MIGRATION_GUIDE.md)

Supported platforms:

- Kotlin/JVM
- Kotlin/JS (IR only)
- Kotlin/Native for iOS
- Kotlin/Native for LinuxX64

This project has very little platform-specific code, and would be easy to port to any other platform.
If you are interested in another platform, we encourage contributions that add the relevant CI configuration to test and deploy for that platform.

## Contribution

To report a problem or request a new feature, [please create an issue](https://gitlab.com/opensavvy/pedestal/-/issues/new).
To learn how to contribute to the project, [please read our wiki](https://gitlab.com/opensavvy/wiki/-/blob/main/README.md).

Pedestal is managed by Gradle, which requires a valid Java installation.
On Windows, substitute `./gradlew` by `gradlew.bat` in the following commands.

Gradle is invoked by running `./gradlew <tasks here>` in the project root.

- `./gradlew projects` to list the available projects,
- `./gradlew tasks` to list the available tasks in the root project,
- `./gradlew <project>:<task>` to execute a task from another project (for example, `./gradlew backbone:tasks` to get
  the list of tasks in the Backbone project).

We recommend using IntelliJ IDEA (Community or Ultimate), for which we provide the configuration (run configurations, coding style…).
