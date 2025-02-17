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

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.recover
import opensavvy.state.outcome.Outcome
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmInline

@JvmInline
@RaiseDSL
value class OutcomeDsl<Failure>(private val raise: Raise<Failure>) :
	Raise<Failure> by raise {

	@RaiseDSL
	fun <T> Outcome<Failure, T>.bind(): T = when (this) {
		is Outcome.Success -> value
		is Outcome.Failure -> raise.raise(failure)
	}
}

/**
 * Arrow-style DSL to execute a [Raise]-based computation to generate an [Outcome].
 */
@OptIn(ExperimentalTypeInference::class)
@RaiseDSL
inline fun <Failure, Value> out(@BuilderInference block: OutcomeDsl<Failure>.() -> Value): Outcome<Failure, Value> =
	recover(
		block = { Outcome.Success(block(OutcomeDsl(this))) },
		recover = { e: Failure -> Outcome.Failure(e) },
	)

/**
 * Arrow-style DSL to execute a [Raise]-based computation to generate an [Outcome].
 */
@OptIn(ExperimentalTypeInference::class)
@RaiseDSL
inline fun <Failure, Value> Raise<Failure>.out(@BuilderInference block: OutcomeDsl<Failure>.() -> Value): Value =
	opensavvy.state.arrow.out(block).toEither().bind()
