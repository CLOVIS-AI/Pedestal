package opensavvy.state

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import opensavvy.state.slice.Slice
import opensavvy.state.slice.valueOrNull
import opensavvy.state.slice.valueOrThrow

suspend fun <T : Any> Flow<Slice<T>>.firstValueOrNull() = firstOrNull()?.valueOrNull

suspend fun <T> Flow<Slice<T>>.firstValueOrThrow() = first().valueOrThrow
