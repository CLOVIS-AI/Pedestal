# Module state

Small library for state management based on KotlinX.Coroutines and Kotlin Arrow Core.

Pedestal State is a centralized way of handling:

- error management using [Either][arrow.core.Either], with integrated API failure reasons,
- reporting the progress of ongoing operations to their callers, entirely through coroutines.

Example usage:

```kotlin
suspend fun setPassword(
	userId: String,
	oldPassword: String,
	repeatOldPassword: String,
	newPassword: String,
) = out {
	// The 'out' builder is essentially the same as the 'either {}' block, 
	// except it intercepts some exceptions used in idiomatic Kotlin 
	// (IllegalArgumentException…)

	// We can easily report the progress of our function
	// The progress is sent directly to the caller using CoroutineContext
	// (does nothing if the caller has not registered a listener)
	report(loading(0.1)) // 10%

	// Use APIs inspired by the standard library's 'require'
	// to check preconditions/invariants
	// The added information can be used for example by the Pedestal Spine module
	// to automatically generate the appropriate HTTP status codes
	ensureAuthenticated(userService.currentUser() == userId) { "You cannot edit the password of another user" }
	ensureValid(oldPassword == repeatOldPassword) { "The two provided passwords are different" }
	ensureValid(oldPassword != newPassword) { "The new password cannot be the same password as the old one" }

	// The goal of the progress reporting API is that you can add
	// reporting points after your application is developed, as your usage
	// dictates they become useful, without any impact to your APIs
	report(loading(0.5)) // 50%

	// Within the block, all progress events go through the provided computation
	transformQuantifiedProgress({ loading(it.normalized / 2.0 + 0.5) }) {
		report(loading(0.1)) // 10%, becomes 55% due to the transformation block

		// We're still inside the Arrow 'either {}' block, so we can use 'bind'
		// and all other utilities
		userService.setPassword(userId, newPassword).bind()

		report(loading(0.8)) // 80%, becomes 90% due to the transformation block
	}
}
```

You can also create your own progress type to store any kind of additional information (e.g. bandwidth speed, estimated time of end, amount of files impacted…) by implementing the [Progression.Quantified][opensavvy.state.Progression.Quantified] interface.

# Package opensavvy.state

Centralized [Failure][opensavvy.state.Failure] management as well as [Progression][opensavvy.state.Progression] APIs to represent ongoing operations and their current state.

# Package opensavvy.state.outcome

Utilities for the [Outcome][opensavvy.state.outcome.Outcome] type, allowing to embed typed error management directly into the API without using exceptions.

# Package opensavvy.state.progressive

The [ProgressiveOutcome][opensavvy.state.progressive.ProgressiveOutcome], which combines [Outcome][opensavvy.state.outcome.Outcome] with [Progression][opensavvy.state.Progression].
