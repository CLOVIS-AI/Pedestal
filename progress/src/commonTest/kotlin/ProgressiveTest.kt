/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.progress

import opensavvy.prepared.runner.testballoon.preparedSuite

@OptIn(ExperimentalProgressApi::class)
val ProgressiveTest by preparedSuite {

	test("toString with progress") {
		check(Progressive(5, loading(0.2)).toString() == "5 Loading(20%)")
	}

	test("toString with text") {
		check(Progressive("foo", done()).toString() == "foo Done")
	}

	test("toString with decimal and progress") {
		check(Progressive(5.2, loading(0.1)).toString() == "5.2 Loading(10%)")
	}

	test("toString with boolean and progress") {
		check(Progressive(true, loading(0.9)).toString() == "true Loading(90%)")
	}

}
