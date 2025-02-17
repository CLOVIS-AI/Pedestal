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

package opensavvy.state.outcome

import opensavvy.state.outcome.Outcome.Success

// region Get or null

/**
 * Returns [Success.value], or `null` if this outcome is not successful.
 */
val <Value : Any> Outcome<*, Value>.valueOrNull: Value?
	get() = (this as? Success)?.value

/**
 * Returns [Failure.failure][Outcome.Failure.failure], or `null` if this outcome is not a failure.
 */
val <Failure : Any> Outcome<Failure, *>.failureOrNull: Failure?
	get() = (this as? Outcome.Failure)?.failure

// endregion
// region Safe get via Nothing

/**
 * Returns [Success.value].
 */
val <Value> Outcome<Nothing, Value>.value: Value
	// This cast is safe, because a Failure of Nothing is impossible
	get() = (this as Success).value

/**
 * Returns [Failure.failure][Outcome.Failure.failure].
 */
val <Failure> Outcome<Failure, Nothing>.failure: Failure
	// This cast is safe, because a Success of Nothing is impossible
	get() = (this as Outcome.Failure).failure

// endregion
