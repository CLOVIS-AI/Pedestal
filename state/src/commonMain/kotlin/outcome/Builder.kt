package opensavvy.state.outcome

fun <T> T.success() = Outcome.Success(this)

fun <F> F.failed() = Outcome.Failure(this)
