# Pedestal Logger: a simple multiplatform logger

This module introduces a `Logger` interface which can easily be used in Kotlin Multiplatform code:
```kotlin
import opensavvy.logger.loggerFor

class Foo {
    init {
        log.trace { "This is a simple tracing message!" }
    }
    
    companion object {
        private val log = loggerFor(this)
    }
}
```
