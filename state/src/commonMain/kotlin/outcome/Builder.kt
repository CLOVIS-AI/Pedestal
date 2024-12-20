package opensavvy.state.outcome

fun <Value> Value.successful() = Outcome.Success(this)

fun <Failure> Failure.failed() = Outcome.Failure(this)
