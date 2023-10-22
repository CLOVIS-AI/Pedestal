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
