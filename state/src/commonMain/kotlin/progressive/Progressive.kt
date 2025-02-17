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
import opensavvy.progress.done
import opensavvy.progress.loading
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.Failure

/**
 * A [Outcome] with integrated [Progress] management.
 *
 * There are three possible cases:
 * - [Incomplete] if the task has started but no value is currently available,
 * - [Success] if a successful result is available (see [Success.value]),
 * - [Failure] if a failed result is available (see [Failure.failure]).
 *
 * Note that in cases, a progressive outcome may be currently loading.
 * - [Incomplete] must be loading,
 * - [Success] may be loading if the task is currently querying a newer value than the one it stores,
 * - [Failure] may be loading if the task is currently retrying the operation.
 *
 * To access the inner outcome and progression, you can use the destructuring operator:
 * ```kotlin
 * val (out, progression) = /* ProgressiveOutcome */
 * ```
 *
 * To create progressive outcomes from computations, use the [successfulWithProgress] and [failedWithProgress] factories.
 */
sealed class ProgressiveOutcome<out Failure, out Value> {

	/**
	 * The current progression of this outcome.
	 *
	 * For more information, see [ProgressiveOutcome].
	 */
	abstract val progress: Progress

	/**
	 * Operations that are not successful; common supertype of [Incomplete] and [Failure].
	 *
	 * Note that a [Success] value with progress information is not included in this hierarchy, as it is successful.
	 *
	 * Due to backwards-compatibility considerations, this cannot be a subtype of [ProgressiveOutcome].
	 * If you have an instance of this interface and need a [ProgressiveOutcome], use [upcast].
	 */
	@ExperimentalProgressiveRaiseApi
	sealed interface Unsuccessful<out Failure>

	/**
	 * The operation is ongoing, but we do not know if it will be successful or a failure.
	 */
	@OptIn(ExperimentalProgressiveRaiseApi::class)
	data class Incomplete(
		override val progress: Progress.Loading = loading(),
	) : ProgressiveOutcome<Nothing, Nothing>(), Unsuccessful<Nothing>

	/**
	 * The latest known result of the operation was a success, available as [value].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	data class Success<Value>(
		val value: Value,
		override val progress: Progress = done(),
	) : ProgressiveOutcome<Nothing, Value>()

	/**
	 * The latest known result of the operation was a failure, available as [failure].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	@OptIn(ExperimentalProgressiveRaiseApi::class)
	data class Failure<Failure>(
		val failure: Failure,
		override val progress: Progress = done(),
	) : ProgressiveOutcome<Failure, Nothing>(), Unsuccessful<Failure>

}
