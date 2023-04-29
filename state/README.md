# Module state

Small library for value-based outcome representation.

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

Originally, `state` was built upon Arrow. Today, `state` is independent of Arrow, but it doesn't duplicate all the nice
tooling provided by Arrow: we recommend using our provided `state-arrow` compatibility layer to use in your
projects. `state` itself is kept as small as possible to help interoperability with projects that do not use Arrow.

There are multiple recommended ways to represent outcomes:

- success, failures and progress in a wrapper object (
  see [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome]),
- success and failures in a wrapper object (see `state-arrow`'s `Outcome`) and progress by asynchronous context calls (
  see `progress-coroutines`'s `CoroutineProgressReporter`),
- success as a regular return type, failures as a context receiver (see Arrow 2.0's `Raise` interface) and progress as a
  context receiver (using `progress`' `ProgressReporter`).

# Package opensavvy.state.outcome

Utilities for the [Outcome][opensavvy.state.outcome.Outcome] type, allowing to embed typed error management directly
into the API without using exceptions.

# Package opensavvy.state.progressive

The [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome], which combines [Outcome][opensavvy.state.outcome.Outcome] with [Progress][opensavvy.progress.Progress].
