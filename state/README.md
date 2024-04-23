# Module State

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
notably [Arrow Typed Errors](https://arrow-kt.io/learn/typed-errors/working-with-typed-errors/). These libraries provide enhanced semantics and
syntax sugar for this style of error management, but they lack a representation for intermediate values (for example,
the current progress of an information).

Pedestal State is built on top of Arrow, and adds:
- Clearly named [success][opensavvy.state.outcome.Outcome.Success] and [failure][opensavvy.state.outcome.Outcome.Failure] cases, instead of [Right][arrow.core.Either.Right] and [Left][arrow.core.Either.Left],
- Support for [in-progress results][opensavvy.state.progressive.ProgressiveOutcome],
- Helpers to handle failed operations in reactive contexts (e.g. Compose): [onSuccess][opensavvy.state.progressive.onSuccess], [onFailure][opensavvy.state.progressive.onFailure], [onIncomplete][opensavvy.state.progressive.onIncomplete], [onLoading][opensavvy.state.progressive.onLoading],
- Helpers for safe access: [value][opensavvy.state.progressive.value], [failure][opensavvy.state.progressive.failure].

## Example

Let's compare a simple account creation screen; we want to verify that the data is correct and display the results to a user.
First, let's see how it could be implemented with the traditional Kotlin approach of using a sealed hierarchy:

```kotlin
// ①. Declare all possible outcomes
sealed class AccountCreationResult {
	data class Success(val user: User) : AccountCreationResult()
	data object PasswordTooShort : AccountCreationResult()
	data object PasswordsDoNotMatch : AccountCreationResult()
	// …
}

// ②. Implement the method
suspend fun createAccount(username: String, password: String, passwordCopy: String): AccountCreationResult {
	// …data checking…
	if (password.length < 4)
		return AccountCreationResult.PasswordTooShort
	// note that the conditions are reversed from what we want!
	
	if (password != passwordCopy)
		return AccountCreationResult.PasswordsDoNotMatch
	
	// …
	
	return AccountCreationResult.Success(repository.createAccount(username, password))
}

// ③. Call site
when (val result = createAccount(username, password, passwordCopy)) {
	is Success -> {
		println("Created the user $it")
	}
	else -> {
		println("Could not create user: $it")
	}
}
```

Now, let's rewrite this using State:
```kotlin
// ①. Declare the failure reasons (no need to declare the successful case)
sealed class AccountCreationFailure {
	data object PasswordTooShort : AccountCreationFailure()
	data object PasswordsDoNotMatch : AccountCreationFailure()
}

// ②. Implement the method
suspend fun createAccount(username: String, password: String, passwordCopy: String) = out<AccountCreationFailure, User> {
	// …data checking…
	ensure(password.length >= 4) { AccountCreationFailure.PasswordTooShort }
	ensure(password != passwordCopy) { AccountCreationFailure.PasswordsDoNotMatch }
	// note that the conditions are in a better order
	
	// …
	
	repository.createAccount(username, password) // no boilerplate
}

// ③. Call site
val result = createAccount(username, password, passwordCopy)

result.onSuccess {
	println("Created the user $it")
}

result.onFailure {
	println("Could not create user: $it")
}
```

As we can see, validation is much easier to read, and the call site offers more flexibility.

# Package opensavvy.state.arrow

Helpers to convert outcomes to other Arrow data types, support for the Raise DSL.

# Package opensavvy.state.outcome

Utilities for the [Outcome][opensavvy.state.outcome.Outcome] type, allowing to embed typed error management directly
into the API without using exceptions.

# Package opensavvy.state.progressive

The [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome], which combines [Outcome][opensavvy.state.outcome.Outcome] with [Progress][opensavvy.progress.Progress].
