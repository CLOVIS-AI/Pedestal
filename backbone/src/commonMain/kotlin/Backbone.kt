package opensavvy.backbone

import opensavvy.state.failure.Failure

/**
 * The parent interface for an implementation of the Backbone pattern.
 *
 * For more information on the Backbone pattern, please read the module-level documentation.
 *
 * @param O The object this backbone manages.
 * @param F Failures that may be returned when calling [Ref.request].
 * @param R The reference responsible for the object [O].
 */
interface Backbone<R : Ref<F, O>, F : Failure, O> {

	companion object
}
