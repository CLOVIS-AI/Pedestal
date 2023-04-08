# Module progress

Micro-library to represent task progress.

The `progress-coroutines` companion library allows Kotlin coroutines to report progress to their callers.

### Usage

```kotlin
import opensavvy.progress.*

println("Current progress: ${done()}")       // Current progress: Done
println("Current progress: ${loading()}")    // Current progress: Loading
println("Current progress: ${loading(0.5)}") // Current progress: Loading(50%)
```

For more information, see [done][opensavvy.progress.done] and [loading][opensavvy.progress.loading].

### Introduction

Progress information is organized around the [Progress][opensavvy.progress.Progress] interface.

- [Done][opensavvy.progress.Progress.Done] (singleton): the work is over
- [Loading][opensavvy.progress.Progress.Loading]: the work is ongoing
  - [Unquantified][opensavvy.progress.Progress.Loading.Unquantified] (singleton): we have no knowledge of its
    progress
  - [Quantified][opensavvy.progress.Progress.Loading.Quantified]: we have some knowledge of its progress. You
    can implement this interface to add new information.

The SAM interface [ProgressReporter][opensavvy.progress.report.ProgressReporter] can be injected into a
sub-task to report progress information to a parent task, for example using context receivers, or through the coroutine
context via the companion library `progress-coroutines`.
