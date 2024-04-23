package opensavvy.state.arrow

import arrow.core.raise.Raise
import arrow.core.raise.RaiseDSL
import arrow.core.raise.recover
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.successfulWithProgress
import opensavvy.state.progressive.upcast
import opensavvy.state.progressive.withProgress
import kotlin.js.JsName
import kotlin.jvm.JvmInline

@JvmInline
@RaiseDSL
@ExperimentalProgressiveRaiseApi
value class ProgressiveOutcomeDsl<Failure>(private val raise: Raise<ProgressiveOutcome.Unsuccessful<Failure>>) : Raise<Failure> {

	override fun raise(r: Failure): Nothing =
		raise(r, done())

	@RaiseDSL
	@JsName("raiseUnsuccessful")
	fun raise(failure: ProgressiveOutcome.Unsuccessful<Failure>): Nothing =
		raise.raise(failure)

	@RaiseDSL
	@JsName("raiseWithProgress")
	fun raise(failure: Failure, progress: Progress = done()): Nothing =
		raise(ProgressiveOutcome.Failure(failure, progress))

	@RaiseDSL
	fun <T> Outcome<Failure, T>.bind(progress: Progress = done()) =
		this.withProgress(progress).bind()

	@RaiseDSL
	fun <T> ProgressiveOutcome<Failure, T>.bind(): T = when (this) {
		is ProgressiveOutcome.Success -> value
		is ProgressiveOutcome.Unsuccessful<*> -> {
			@Suppress("UNCHECKED_CAST") // safe because it is guaranteed by the type bind on the receiver
			raise.raise(this as ProgressiveOutcome.Unsuccessful<Failure>)
		}
	}
}

/**
 * Arrow-style DSL to execute a [Raise]-based computation to generate a [ProgressiveOutcome].
 *
 * **Warning:** the current implementation **does not** capture progress events fired during [block].
 */
@ExperimentalProgressiveRaiseApi
@RaiseDSL
inline fun <Failure, Value> progressive(block: ProgressiveOutcomeDsl<Failure>.() -> Value): ProgressiveOutcome<Failure, Value> =
	recover(
		block = {
			block(ProgressiveOutcomeDsl(this))
				.successfulWithProgress(done())
		},
		recover = { it.upcast() },
	)
