package opensavvy.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import opensavvy.state.slice.Slice
import opensavvy.state.slice.failed

suspend fun <T> Flow<Slice<T>>.firstValue() = firstOrNull()
	?: failed("The flow terminated before emitting a value", Failure.Kind.NotFound)
