package opensavvy.state.arrow

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.recover
import opensavvy.state.outcome.Outcome
import kotlin.experimental.ExperimentalTypeInference
import kotlin.jvm.JvmInline

@JvmInline
@RaiseDSL
value class OutcomeDsl<F>(private val raise: Raise<F>) :
    Raise<F> by raise {

    @RaiseDSL
    fun <T> Outcome<F, T>.bind(): T = when (this) {
        is Outcome.Success -> value
        is Outcome.Failure -> raise.raise(failure)
    }
}

/**
 * Arrow-style DSL to execute a [Raise]-based computation to generate an [Outcome].
 */
@OptIn(ExperimentalTypeInference::class)
@RaiseDSL
inline fun <F, T> out(@BuilderInference block: OutcomeDsl<F>.() -> T): Outcome<F, T> =
    recover(
        block = { Outcome.Success(block(OutcomeDsl(this))) },
        recover = { e: F -> Outcome.Failure(e) },
    )
