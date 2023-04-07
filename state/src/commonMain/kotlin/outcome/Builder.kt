package opensavvy.state.outcome

import opensavvy.state.failure.Failure

fun <T> T.success() = Outcome.Success(this)

fun <F : Failure> F.failed() = Outcome.Failure(this)
