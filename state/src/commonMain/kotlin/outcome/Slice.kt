package opensavvy.state.outcome

import arrow.core.Either
import opensavvy.state.Failure

/**
 * The result of a calculation that may have failed.
 *
 * [Outcome] is an implementation of the [Functional Error Handling](https://arrow-kt.io/docs/patterns/error_handling/)
 * pattern recommended by Arrow.
 */
typealias Outcome<T> = Either<Failure, T>
