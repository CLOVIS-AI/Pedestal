# Module progress-coroutines

Compatibility layer for `progress` and KotlinX.coroutines.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22progress-coroutines%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/progress-coroutines.svg?label=Maven%20Central"></a>

Using the utility functions declared in this project, it is possible to report progress events from any coroutine
without changing its signature. Simply call the [report][opensavvy.progress.coroutines.report] function:

```kotlin
import opensavvy.progress.coroutines.*

suspend fun foo() {
    report(loading(0.0)) // 0% loading

    someExpensiveComputation()
    report(loading(0.5)) // 50% loading

    someOtherExpensiveComputation()
    report(loading(1.0)) // 100% loading
}
```

Reporting progression events only has an effect if a parent coroutine is listening to them. Otherwise, the `report`
function does nothing—ensuring the behavior of your program is not changed when you introduce progress reporting
step-by-step in your codebase.

Any [ProgressReporter][opensavvy.progress.report.ProgressReporter] can listen to `report` calls, via
the [asCoroutineContext][opensavvy.progress.coroutines.asCoroutineContext] conversion function which converts
it into a [CoroutineContext][kotlin.coroutines.CoroutineContext] element:

```kotlin
suspend fun main() {
    val reporter = ProgressReporter { println("Progress: $it") }
        .asCoroutineContext()

    withContext(reporter) {
        task()
    }
}

suspend fun task() {
    report(loading(0.2))
    delay(1000)
    report(loading(0.7))
}
```

```text
Progress: Loading(20%)
Progress: Loading(70%)
```

The same result can be achieved using the
convenience [reportProgress][opensavvy.progress.coroutines.reportProgress] function:

```kotlin
suspend fun main() {
    reportProgress({ println("Progress: $it") }) {
        task()
    }
}
```

Instead of reacting with a callback on progress events, it is also possible to collect events into
a [StateFlow][kotlinx.coroutines.flow.StateFlow] using [StateFlowProgressReporter][opensavvy.progress.coroutines.StateFlowProgressReporter].

Finally, sub-tasks can be represented with the help
of [mapProgressTo][opensavvy.progress.coroutines.mapProgressTo]:

```kotlin
suspend fun main() {
    reportProgress({ println("Progress: $it") }) {
        task()
    }
}

suspend fun task() {
    report(loading(0.0))

    mapProgressTo(0.2..0.5) {
        println("Task 1")
        task1()
    }

    mapProgressTo(0.5..1.0) {
        println("Task 2")
        task2()
    }

    report(done())
}

suspend fun task1() {
    report(loading(0.0))
    delay(1000)
    report(done())
}

suspend fun task2() {
    report(loading(0.0))
    delay(500)
    report(loading(0.5))
    delay(500)
    report(done())
}
```

```text
Progress: Loading(0%)
Task 1
Progress: Loading(20%)
Progress: Loading(50%)
Task 2
Progress: Loading(50%)
Progress: Loading(75%)
Progress: Loading(100%)
Progress: Done
```
