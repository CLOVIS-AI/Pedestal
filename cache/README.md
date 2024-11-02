# Module Cache

Pedestal Cache is a collection of in-process cache algorithms.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22cache%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/cache.svg?label=Maven%20Central"></a>
<a href="https://opensavvy.dev/open-source/stability.html"><img src="https://badgen.net/static/Stability/stable/purple"></a>
<a href="https://javadoc.io/doc/dev.opensavvy.pedestal/cache"><img src="https://badgen.net/static/Other%20versions/javadoc.io/blue"></a>

Caching is a powerful technique which allows to dramatically reduce I/O requests, making applications faster and more responsive.
For more information on our recommendations for cache-aware software architecture, see the Backbone module.

This library provides multiplatform cache algorithms, which can be used in the same manner client- and server-side. Based on coroutines, they are suitable for high-performance applications as well as mobile web pages. Our goal is to keep this library as lightweight as possible.

## Cache operations

Caches behave as key-value stores which are able to query the value automatically. The essential operations are:

- `get`: to request the value behind a key,
- `expire`: to notify the cache that we know the value has changed,
- `update`: to notify the cache that we know what the most recent value for a given key is.

Additionally, `get` returns a long-lived [Flow][kotlinx.coroutines.flow.Flow] you can subscribe to, to be notified of future changes to the value. This is particularly useful when using reactive UI frameworks (React, Compose…), as it allows to directly communicate to the framework which parts of the UI change, meaning all components reading from the cache automatically update themselves on new values, with no additional code.

## Layering

Complex caching strategies are created by composing simpler implementations. Here is an example of a cache which will store values in-memory for 10 minutes:

```kotlin
val scope: CoroutineScope = TODO()

val powersOfTwo = cache<Int, Int> { it * 2 }
	.cachedInMemory(scope.coroutineContext.job)
	.expireAfter(10.minutes, scope, clock)

println(powersOfTwo[5].now())
```

The initial layer (here, the `cache` factory) is an adapter to expose a regular function as a cache instance, to bootstrap layering. It doesn't do any caching itself.

# Package opensavvy.cache

Reactive key-value store algorithms represented by the [`Cache`][opensavvy.cache.Cache] interface.

For convenience, adapters are provided:

- [`cache`][opensavvy.cache.cache] caches calls to any `suspend` function,
- [`batchingCache`][opensavvy.cache.batchingCache] intercepts subsequent cache requests and batches them into a single larger request.

The following cache layers are available:

- [`cachedInMemory`][opensavvy.cache.cachedInMemory] stores results in-memory. When values are updated, it continues to show the old values while the new ones are being fetched,
- [`expireAfter`][opensavvy.cache.expireAfter] expires the values stored by the previous layer after some time has passed.

# Package opensavvy.cache.contextual

Reactive key-context-value store algorithms represented by the [`ContextualCache`][opensavvy.cache.contextual.ContextualCache] interface.

For convenience, adapters are provided:

- [`cache`][opensavvy.cache.contextual.cache] caches calls to any `suspend` function,
- [`batchingCache`][opensavvy.cache.contextual.batchingCache] intercepts subsequent cache requests and batches them into a single larger request.

The following cache layers are available:

- [`cachedInMemory`][opensavvy.cache.contextual.cachedInMemory] stores results in-memory. When values are updated, it continues to show the old values while the new ones are being fetched,
- [`expireAfter`][opensavvy.cache.contextual.expireAfter] expires the values stored by the previous layer after some time has passed.
