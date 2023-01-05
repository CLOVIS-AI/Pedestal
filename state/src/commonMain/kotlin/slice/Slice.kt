package opensavvy.state.slice

import arrow.core.Either
import opensavvy.state.Failure

/**
 * The result of a calculation that may have failed.
 *
 * [Slice] is an implementation of the [Functional Error Handling](https://arrow-kt.io/docs/patterns/error_handling/)
 * pattern recommended by Arrow.
 */
typealias Slice<T> = Either<Failure, T>
