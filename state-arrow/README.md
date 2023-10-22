# Module State (Arrow compatibility)

Compatibility layer for `state` and [Arrow Core](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/).

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22state-arrow%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/state-arrow.svg?label=Maven%20Central"></a>

<a href="https://gitlab.com/opensavvy/wiki/-/blob/main/stability.md#stability-levels"><img src="https://badgen.net/static/Stability/stable/purple"></a>

The `state` project is an error management framework for immutable data with progress management using `progress`.
Arrow Core is particularly great at representing business failures, and `state` was created to be completely compatible.
We encourage any project that has the option to use Arrow Core alongside `state`, using this compatibility module.
