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
import opensavvy.state.progressive.ProgressiveOutcome.Incomplete
import opensavvy.state.progressive.ProgressiveOutcome.Success

/**
 * Executes [block] if this outcome is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <Value> ProgressiveOutcome<*, Value>.onSuccess(block: (Value) -> Unit) {
	if (this is Success<Value>)
		block(this.value)
}

/**
 * Executes [block] if this outcome is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <Failure> ProgressiveOutcome<Failure, *>.onFailure(block: (Failure) -> Unit) {
	if (this is ProgressiveOutcome.Failure)
		block(this.failure)
}

/**
 * Executes [block] if this outcome is [incomplete][Incomplete].
 *
 * Otherwise, does nothing.
 */
inline fun ProgressiveOutcome<*, *>.onIncomplete(block: () -> Unit) {
	if (this is Incomplete)
		block()
}

/**
 * Executes [block] if this outcome is loading (its [ProgressiveOutcome.progress] is [Progress.Loading]).
 *
 * Note that this isn't synonymous with this outcome being in the [Incomplete] state: successful or failed outcomes may
 * still be loading. For more information, see [ProgressiveOutcome].
 *
 * Otherwise, does nothing.
 */
inline fun ProgressiveOutcome<*, *>.onLoading(block: (Progress.Loading) -> Unit) {
	val progression = progress

	if (progression is Progress.Loading)
		block(progression)
}
