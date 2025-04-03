# Module Logger (DEPRECATED)

Simple multiplatform [Logger][opensavvy.logger.Logger] interface that delegates to the logging facilities of the underlying platform.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22logger%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/logger.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/archived/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.pedestal/logger"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

## Deprecation warning

This library has been deprecated. For now, all implementations have fallen back to using the standard library's `println`. No more useful versions will be published. We do not plan on adding any new features, but the library won't be removed to avoid breaking existing projects.

If you are using this library, we encourage to migrate to other more popular logging libraries, like KotlinLogging.

## Usage

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
