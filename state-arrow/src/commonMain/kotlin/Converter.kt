package opensavvy.state.arrow

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import opensavvy.state.failure.Failure
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.success
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.withProgress

/**
 * Converts an [Outcome] into a typed [Either].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <F : Failure, T> Outcome<F, T>.toEither() = when (this) {
    is Outcome.Success -> value.right()
    is Outcome.Failure -> failure.left()
}

/**
 * Converts a [ProgressiveOutcome] into a typed [Either].
 *
 * If the outcome is [incomplete][ProgressiveOutcome.Incomplete], `null` is returned.
 */
fun <F : Failure, T> ProgressiveOutcome<F, T>.toEither() = when (this) {
    is ProgressiveOutcome.Success -> value.right()
    is ProgressiveOutcome.Failure -> failure.left()
    is ProgressiveOutcome.Incomplete -> null
}

/**
 * Converts an [Either] into an [Outcome].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <F : Failure, T> Either<F, T>.toOutcome() = when (this) {
    is Either.Right -> value.success()
    is Either.Left -> value.failed()
}

/**
 * Converts an [Either] into a [ProgressiveOutcome].
 *
 * The [out] DSL is also useful to work with [Outcome] in the Arrow world.
 */
fun <F : Failure, T> Either<F, T>.toOutcome(progress: Progress = loading()) = toOutcome()
    .withProgress(progress)
