package opensavvy.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed

suspend fun <T> Flow<Outcome<T>>.firstValue() = firstOrNull()
	?: failed("The flow terminated before emitting a value", Failure.Kind.NotFound)
