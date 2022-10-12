# Pedestal • Software architecture utilities

OpenSavvy Pedestal is a collection of open source utilities for unified software architecture, aiming to solve common problems in a simple and elegant way with Kotlin.
In particular, Pedestal focuses on fullstack development.

This project contains the following modules:

- [Pedestal Logger](logger/README.md) is a simple logger implementation for multiplatform projects.
- [Pedestal State](state/README.md) is a state management library based on KotlinX.Coroutines to represent values that change over time.
- [Pedestal Cache](cache/README.md) is a collection of cache implementations for Pedestal State which helps with reducing network traffic on the entire stack.
- [Pedestal Backbone](backbone/README.md) helps with exposing multiple implementations of the same API transparently, facilitating architectural modifications with automatic caching and state management for performant reactive applications.
- [Pedestal Spine](spine/README.md) declares typesafe Kotlin API endpoints that can be easily used from any web server or client.

Pedestal focuses heavily on Kotlin and depends on some KotlinX libraries (e.g. Coroutines).
Apart from that, Pedestal tries to be as agnostic of the technology as possible: for example, Spine APIs can be implemented with any web client or server framework (the Ktor implementation is available out of the box).

## Development

Pedestal is managed by Gradle, which requires a valid Java installation.
On Windows, substitute `./gradlew` by `gradlew.bat` in the given commands.

Gradle is invoked by running `./gradlew <tasks here>` in the project root.

- `./gradlew projects` to list the available projects,
- `./gradlew tasks` to list the available tasks in the root project,
- `./gradlew <project>:<task>` to execute a task from another project (for example, `./gradlew backbone:tasks` to get the list of tasks in the Backbone project).
