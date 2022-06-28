# Pedestal • Software architecture utilities

OpenSavvy Pedestal is a collection of open source utilities for unified software architecture, aiming to solve common problems in a simple and elegant way.

- [Pedestal Backbone](backbone/README.md) unifies API architecture by including caching and efficient transfers directly into the API itself.
- Pedestal Spine (TODO) allows Backbone implementations to interact with each other transparently through a network boundary.
- [Pedestal Logger](logger/README.md) is a simple logger implementation for multiplatform projects.

## Development

Pedestal is managed by Gradle, which requires a valid Java installation.
On Windows, substitute `./gradlew` by `gradlew.bat` in the given commands.

Gradle is invoked by running `./gradlew <tasks here>` in the project root.
- `./gradlew projects` to list the available projects,
- `./gradlew tasks` to list the available tasks in the root project,
- `./gradlew <project>:<task>` to execute a task from another project (for example, `./gradlew backbone:tasks` to get the list of tasks in the Backbone project).
