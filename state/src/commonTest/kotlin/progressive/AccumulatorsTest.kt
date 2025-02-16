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

package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading

class AccumulatorsTest : PreparedSpec({

	test("Iterable") {
		val actual = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		).combineCompleted()
			.toList()

		val expected = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Success(1, loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Failure(Unit, loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)

		check(actual == expected)
	}

	test("Sequence") {
		val actual = sequenceOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)
			.combineCompleted()
			.toList()

		val expected = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Success(1, loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Failure(Unit, loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)

		check(actual == expected)
	}
})
