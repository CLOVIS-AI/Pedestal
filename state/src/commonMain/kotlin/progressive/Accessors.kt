/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.state.progressive

import opensavvy.progress.Progress
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.*

// region Get or null

/**
 * Returns [Success.value], or `null` if this outcome is not successful.
 */
val <Value : Any> ProgressiveOutcome<*, Value>.valueOrNull: Value?
	get() = (this as? Success)?.value

/**
 * Returns [Failure.failure][ProgressiveOutcome.Failure.failure], or `null` if this outcome is not a failure.
 */
val <Failure> ProgressiveOutcome<Failure, *>.failureOrNull: Failure?
	get() = (this as? ProgressiveOutcome.Failure)?.failure

// endregion
// region Safe get via Nothing

/**
 * Returns [Success.value].
 */
val <Value> ProgressiveOutcome<Nothing, Value>.value: Value
	// This cast is safe, because a Success of Nothing is impossible
	get() = (this as Success).value

/**
 * Returns [Failure.failure][ProgressiveOutcome.Failure.failure].
 */
val <Failure> ProgressiveOutcome<Failure, Nothing>.failure: Failure
	// This cast is safe, because a Success of Nothing is impossible
	get() = (this as ProgressiveOutcome.Failure).failure

/**
 * The progression of this unsuccessful value.
 *
 * See [ProgressiveOutcome].
 */
@ExperimentalProgressiveRaiseApi
val <Failure> Unsuccessful<Failure>.progress: Progress
	get() = when (this) {
		is ProgressiveOutcome.Failure -> progress
		is Incomplete -> progress
	}

// endregion
// region Conversion

/**
 * Converts this progressive outcome into a [regular outcome][Outcome].
 *
 * Because regular outcomes do not have a concept of progression, the progress information is lost.
 * To access both the outcome and the progression information, consider using destructuration instead:
 * ```kotlin
 * val (outcome, progression) = /* ProgressiveOutcome */
 * ```
 */
fun <Failure, Value> ProgressiveOutcome<Failure, Value>.asOutcome() = when (this) {
	is Success -> Outcome.Success(value)
	is ProgressiveOutcome.Failure -> Outcome.Failure(failure)
	is Incomplete -> null
}

// endregion
// region Destructuration

/**
 * Syntax sugar for [asOutcome].
 */
operator fun <Failure, Value> ProgressiveOutcome<Failure, Value>.component1() = asOutcome()

/**
 * Syntax sugar for [ProgressiveOutcome.progress].
 */
operator fun ProgressiveOutcome<*, *>.component2() = progress

// endregion
