# Module progress

Micro-library to represent the state of an unfinished task.

It is often recommended to communicate the progress of any task that doesn't finish instantly to the user.
However, this is often forgotten and implemented as a last-minute addition.
Pedestal Progress brings a lightweight and easy to use solution.

Coroutines-based application can use the `progress-coroutines` companion library to include progress reporting as part of structured concurrency, meaning progress reporting can be added after-the-fact.

## Three kinds of progress events

An ongoing task can be either done or loading, in which case we may or may not have any information on its current status.
These relationships are represented by the [`Progress`][opensavvy.progress.Progress] interface hierarchy.

### Done: the task is over

When a task is over, it has no progress to communicate. This is represented by the [`Done`][opensavvy.progress.Progress.Done] singleton.

For convenience, we can instantiate it with the [`done`][opensavvy.progress.done] helper:
```kotlin
import opensavvy.progress.*

println("Current progress: ${done()}")       // Current progress: Done
```

### Unquantified loading: the task is progressing at an unknown rate

It is common to know that a task is currently running, but have access to no information on its progress.
This is most commonly the case when we start introducing Pedestal into a new codebase.

Some libraries may also be able to infer that a task is running, without having anymore information about it. For example, this is the case for the asynchronous event handlers of [Decouple](https://gitlab.com/opensavvy/decouple).

This state is represented by the [`Loading.Unquantified`][opensavvy.progress.Progress.Loading.Unquantified] singleton. For convenience, it is also available as the parameterless [`loading`][opensavvy.progress.loading] helper:

```kotlin
import opensavvy.progress.*

println("Current progress: ${loading()}")    // Current progress: Loading
```

### Quantified loading: the task is progressing at a known rate

Lastly, we can have some information about the progress of a task. At minimum, we should have an estimation of the percentage of completion of the task. This is represented by the [`Loading.Quantified`][opensavvy.progress.Progress.Loading.Quantified] interface, of which the most basic implementation is available as the single-parameter [`loading`][opensavvy.progress.loading] helper:

```kotlin
import opensavvy.progress.*

println("Current progress: ${loading(0.2)}") // Current progress: Loading(20%)
println("Current progress: ${loading(0.5)}") // Current progress: Loading(50%)
```

Because this state is an interface, you are free to create your own implementations to store any additional data that may be useful in your case (estimated time of completion, current bandwidth…). When doing so, we recommend accepting as parameter one of the types of this package, and down-casting when displaying to conditionally add the additional data, to ensure your program can accept other custom implementations from other libraries seamlessly.

## Reporting progress to the parent task

The simplest way to access progress events of a child task from its parent is to pass down an instance of [`ProgressReporter`][opensavvy.progress.report.ProgressReporter]. For example, if a function `caller` wants to receive progress events from a function `child`, it can:

```kotlin
import opensavvy.progress.*
import opensavvy.progress.report.*

fun caller(): Foo {
	val progressReporter = ProgressReporter {
		// Do something when receiving an event, could be anything.
		// In this example, we will just print it to the standard output.
		println("Current progress: $it")
	}

	return child(Random.nexInt(), progressReporter)
}

fun child(
	id: Int,
	// Declare an optional progress reporter
	// 'emptyProgressReporter' is a no-op implementation
	progressReporter: ProgressReporter = emptyProgressReporter(),
): Foo {
	progressReporter.report(loading(0.0))
	val intermediary = doSomething(id)
	progressReporter.report(loading(0.5))
	val result = doSomethingElse(intermediary)
	progressReporter.report(done())
	return result
}
```

There are multiple other ways to pass the progress reporter to the child task. For example, context receivers allow us to write:

```kotlin
fun caller(): Foo {
	// Same as the previous example
	val progressReporter = ProgressReporter(::println)

	return with(progressReporter) {
		child(Random.nextInt())
	}
}

context(ProgressReporter)
fun child(id: Int): Foo {
	report(loading(0.0))
	val intermediary = doSomething(id)
	report(loading(0.5))
	val result = doSomethingElse(intermediary)
	report(done())
	return result
}
```

For applications using coroutines, the companion library `progress-coroutines` offers to store the reporter directly into the coroutine context. For more information, see its documentation.

```kotlin
suspend fun caller(): Foo {
	// Same as the previous example
	val progressReporter = ProgressReporter(::println)

	return withContext(progressReporter.asCoroutineContext()) {
		child(Random.nextInt())
	}
}

suspend fun child(id: Int): Foo {
	report(loading(0.0))
	val intermediary = doSomething(id)
	report(loading(0.5))
	val result = doSomethingElse(intermediary)
	report(done())
	return result
}
```

Progress reporters also offer utilities for managing progress events coming from sequential tasks. For more information, read the package-level documentation.

# Package opensavvy.progress

The [`Progress`][opensavvy.progress.Progress] interface hierarchy.

## Overview

When storing progress events:

- [`Progress`][opensavvy.progress.Progress] represents both finished and ongoing tasks,
- [`Progress.Done`][opensavvy.progress.Progress.Done] represents finished tasks,
- [`Progress.Loading`][opensavvy.progress.Progress.Loading] represents ongoing tasks,
- [`Progress.Loading.Unquantified`][opensavvy.progress.Progress.Loading.Unquantified] represents ongoing tasks for which no more information is available,
- [`Progress.Loading.Quantified`][opensavvy.progress.Progress.Loading.Quantified] represents ongoing tasks for which some information is available.

When reporting events, most of the code should use the [`done`][opensavvy.progress.done] and the [`loading`][opensavvy.progress.loading] helpers.

# Package opensavvy.progress.report

Mechanisms and algorithms to report progress events from subtasks to their parent.

## Overview

The main mechanism to report progress is via the [`ProgressReporter`][opensavvy.progress.report.ProgressReporter] interface.

To transmit a progress reporter to subtasks, use the [`reduceToInterval`][opensavvy.progress.report.reduceToInterval] function.
