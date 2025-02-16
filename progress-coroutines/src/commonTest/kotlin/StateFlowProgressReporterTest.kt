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

package opensavvy.progress.coroutines

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.yield
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.launch
import opensavvy.progress.done
import opensavvy.progress.loading

class StateFlowProgressReporterTest : PreparedSpec({

	test("Reporting progress events through a StateFlow") {
		val reporter = StateFlowProgressReporter()

		launch {
			reporter.report(loading(0.2))
			yield()

			reporter.report(loading(0.3))
			yield()

			check(reporter.toString() == "StateFlowProgressReporter(progress=Loading(30%))")

			reporter.report(done())
			yield()
		}

		val expected = listOf(
			loading(0.0),
			loading(0.2),
			loading(0.3),
			done(),
		)

		check(expected == reporter.progress.transformWhile { emit(it); it != done() }.toList())
	}

})
