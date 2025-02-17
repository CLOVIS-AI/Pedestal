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

package opensavvy.state.outcome

/**
 * If this outcome is [successful][Outcome.Success], replaces its [value][Outcome.Success.value] using [transform].
 *
 * If this outcome isn't successful, does nothing.
 *
 * @see mapFailure Map the failure state instead of the success state.
 */
inline fun <Failure, InputValue, OutputValue> Outcome<Failure, InputValue>.map(transform: (InputValue) -> OutputValue) = when (this) {
	is Outcome.Failure -> this
	is Outcome.Success -> Outcome.Success(transform(this.value))
}

/**
 * If this outcome is [failed][Outcome.Failure], replaces its [failure][Outcome.Failure.failure] using [transformFailure].
 *
 * If this outcome isn't failed, does nothing.
 *
 * @see map Map the success state instead of the failure state.
 */
inline fun <InputFailure, Value, OutputFailure> Outcome<InputFailure, Value>.mapFailure(transformFailure: (InputFailure) -> OutputFailure) = when (this) {
	is Outcome.Failure -> Outcome.Failure(transformFailure(this.failure))
	is Outcome.Success -> this
}
