# Module Logger

Simple multiplatform [Logger][opensavvy.logger.Logger] interface that delegates to the logging facilities of the underlying platform.

- JVM: Slf4J
- JS: `console` API

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22logger%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/logger.svg?label=Maven%20Central"></a>

<a href="https://gitlab.com/opensavvy/wiki/-/blob/main/stability.md#stability-levels"><img src="https://badgen.net/static/Stability/archived/purple"></a>

Example usage:

```kotlin
import opensavvy.logger.loggerFor

class Foo {
	private val log = loggerFor(this)

	init {
		// Simplest possible message: just a string, which is only evaluated 
		// if that logging level is enabled
		log.trace { "This is a simple tracing message!" }

		// Objects placed in parentheses will be displayed as is by the
		// logger if it supports it (e.g. the Chrome DevTools object inspector)
		log.trace(this) { "Current status of the object" }
	}
}
```
