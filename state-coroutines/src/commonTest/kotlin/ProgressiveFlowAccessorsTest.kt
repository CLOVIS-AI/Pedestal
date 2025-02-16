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

import kotlinx.coroutines.flow.flowOf
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome

class ProgressiveFlowAccessorsTest : PreparedSpec({

	data class NotFound(val value: Int)

	test("Failure") {
		val input = flowOf(
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
			ProgressiveOutcome.Failure(NotFound(2)),
		)

		check(input.now() == Outcome.Failure(NotFound(2)))
	}

	test("Success") {
		val input = flowOf(
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
			ProgressiveOutcome.Success(2),
		)

		check(input.now() == Outcome.Success(2))
	}
})
