# Module state

Small library for value-based outcome representation.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22state%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/state.svg?label=Maven%20Central"></a>

<a href="https://gitlab.com/opensavvy/wiki/-/blob/main/stability.md#stability-levels"><img src="https://badgen.net/static/Stability/stable/purple"></a>

Using `state`, it is possible to model a successful operation, a failed operation, as well as the intermediate progress
states of an ongoing operation (using `progress`).

## Representing a computation's outcome

In
Kotlin, [exceptions are not an idiomatic way to represent domain failures](https://elizarov.medium.com/kotlin-and-exceptions-8062f589d07).
They should only be used for programming errors (for example, broken invariants, using `IllegalArgumentException`),
issues that may arise anywhere in a program and thus shouldn't be handled everywhere they appear (for
example, `OutOfMemoryError`) or to encapsulate errors outside our control (for example, `NetworkError`). Instead, we
should represent the outcome of a domain operation using
a [sealed class hierarchy](https://kotlinlang.org/docs/sealed-classes.html).

Multiple libraries have been created to facilitate using these sealed class hierarchies, most
notably [Arrow](https://arrow-kt.io/docs/patterns/error_handling/). These libraries provide enhanced semantics and
syntax sugar for this style of error management, but they lack a representation for intermediate values (for example,
the current progress of an information).

Originally, `state` was built on top of Arrow. Today, `state` is independent of Arrow, but it doesn't duplicate all the nice
tooling provided by Arrow. We recommend using both projects together thanks to our `state-arrow` compatibility layer.
`state` itself is kept as small as possible to help interoperability with projects that do not use Arrow.

There are multiple recommended ways to represent outcomes:

- success, failures and progress in a wrapper object (see [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome]),
- success and failures in a wrapper object (see [Outcome][opensavvy.state.outcome.Outcome]) and progress by asynchronous context calls (see `progress-coroutines`'s `CoroutineProgressReporter`),
- success as a regular return type, failures as a context receiver (see Arrow 2.0's [`Raise`](https://apidocs.arrow-kt.io/arrow-core/arrow.core.raise/-raise/index.html) interface) and progress as a
  context receiver (using `progress`' [ProgressReporter][opensavvy.progress.report.ProgressReporter]).

# Package opensavvy.state.outcome

Utilities for the [Outcome][opensavvy.state.outcome.Outcome] type, allowing to embed typed error management directly
into the API without using exceptions.

# Package opensavvy.state.progressive

The [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome], which combines [Outcome][opensavvy.state.outcome.Outcome] with [Progress][opensavvy.progress.Progress].
