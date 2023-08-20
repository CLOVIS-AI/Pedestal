package opensavvy.state.outcome

@Deprecated("The 'success' builder has been renamed to 'successful'", replaceWith = ReplaceWith("successful()", "opensavvy.state.outcome.successful"))
fun <Value> Value.success() = Outcome.Success(this)

fun <Value> Value.successful() = Outcome.Success(this)

fun <Failure> Failure.failed() = Outcome.Failure(this)
