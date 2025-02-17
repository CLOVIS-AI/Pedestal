/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.state.coroutines

import kotlinx.coroutines.flow.map
import opensavvy.state.progressive.ProgressiveOutcome

@Suppress("DuplicatedCode") // Yes, it's a duplicate, but this one suspends, so it has a different signature
fun <Failure, Value> ProgressiveFlow<Failure, Value>.combineCompleted(): ProgressiveFlow<Failure, Value> {
	var lastComplete: ProgressiveOutcome<Failure, Value>? = null

	return map {
		when (it) {
			is ProgressiveOutcome.Success -> {
				lastComplete = it
				it
			}

			is ProgressiveOutcome.Failure -> {
				lastComplete = it
				it
			}

			is ProgressiveOutcome.Incomplete -> {
				when (val lastCompleteCopy = lastComplete) {
					// No previous completed elements has been stored, just return the incomplete state
					null -> it

					// A previous completed element is stored, return it with the new progress
					is ProgressiveOutcome.Success -> lastCompleteCopy.copy(progress = it.progress)
					is ProgressiveOutcome.Failure -> lastCompleteCopy.copy(progress = it.progress)

					is ProgressiveOutcome.Incomplete -> error("Impossible case: stored an incomplete value in the complete accumulator: $lastCompleteCopy")
				}
			}
		}
	}
}
