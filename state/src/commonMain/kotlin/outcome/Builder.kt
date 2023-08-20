package opensavvy.state.outcome

fun <Value> Value.success() = Outcome.Success(this)

fun <Failure> Failure.failed() = Outcome.Failure(this)
