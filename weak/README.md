# Module Weak

Weak references and maps for Kotlin Multiplatform.

<a href="https://search.maven.org/search?q=g:%22dev.opensavvy.pedestal%22%20AND%20a:%22weak%22"><img src="https://img.shields.io/maven-central/v/dev.opensavvy.pedestal/weak.svg?label=Maven%20Central"></a>

<a href="https://gitlab.com/opensavvy/wiki/-/blob/main/stability.md#stability-levels"><img src="https://badgen.net/static/Stability/alpha/purple"></a>

## Introduction

When we declare a variable in Kotlin, we create a **strong reference** on an object:
```kotlin
class User(val email: String, val age: Int)
val george = User("george@mail.net", 18)
```
The object referred to by the variable `george` must be kept in memory until the variable `george` goes out of scope.

Sometimes, however, we can accept that an object disappears even before we stop referring to it. For example, this may be needed to avoid caches growing out to be larger than the available memory.

Most platforms provide ways to hint to a runtime that an object may be freed even if it is still in use.
This library provides a unified way to give these hints to the runtime.

## Weak references

Weak references hint to the runtime that a value may be freed even if it is in use.
This library provides two main implementations:
- [`WeakRef`][opensavvy.pedestal.weak.WeakRef] hints that a value may be freed as soon as it isn't used elsewhere,
- [`SoftRef`][opensavvy.pedestal.weak.SoftRef] hints that a value will likely to re-used in the future even if it doesn't seem to be used at a specific instant, but may still be removed if the system lacks memory.

## Weak maps

Weak maps are used to create associations between two values that may be freed by the runtime.
Essentially, weak maps are maps that "forget" values. 
Unlike regular maps, they are not iterable, to ensure algorithms cannot depend on their internal state.

The main implementation is [`WeakMap`][opensavvy.pedestal.weak.WeakMap], which weakly holds its keys.

# Package opensavvy.pedestal.weak

Primitive weak data structures and their default (platform-specific) implementation.

# Package opensavvy.pedestal.weak.algorithms

Additional pure-Kotlin implementations that are useful when no platform-specific implementation is available, when the exact same behavior must be provided on all platforms, or to help with testing.
