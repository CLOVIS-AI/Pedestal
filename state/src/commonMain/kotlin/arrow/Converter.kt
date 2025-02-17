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

package opensavvy.state.arrow

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.withProgress

/**
 * Converts an [Outcome] into a typed [Either].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <Failure, Value> Outcome<Failure, Value>.toEither() = when (this) {
	is Outcome.Success -> value.right()
	is Outcome.Failure -> failure.left()
}

/**
 * Converts a [ProgressiveOutcome] into a typed [Either].
 *
 * If the outcome is [incomplete][ProgressiveOutcome.Incomplete], `null` is returned.
 */
fun <Failure, Value> ProgressiveOutcome<Failure, Value>.toEither() = when (this) {
	is ProgressiveOutcome.Success -> value.right()
	is ProgressiveOutcome.Failure -> failure.left()
	is ProgressiveOutcome.Incomplete -> null
}

/**
 * Converts an [Either] into an [Outcome].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <Failure, Value> Either<Failure, Value>.toOutcome() = when (this) {
	is Either.Right -> value.successful()
	is Either.Left -> value.failed()
}

/**
 * Converts an [Either] into a [ProgressiveOutcome].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <Failure, Value> Either<Failure, Value>.toOutcome(progress: Progress = loading()) = toOutcome()
	.withProgress(progress)
