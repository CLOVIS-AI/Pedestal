package opensavvy.spine

import opensavvy.backbone.StateBuilder

typealias OperationValidator<In, Out, Context> = suspend StateBuilder<Out>.(In, context: Context) -> Unit

class Operation<Resource, In, Out, Context>(
	val parent: ResourceGroup.AbstractResource<Resource, Context>,
	val kind: Kind,
	val route: Route? = null,
	val validate: OperationValidator<In, Out, Context>,
) {

	/**
	 * The various kinds of operations that can be executed on a [ResourceGroup.AbstractResource] instance.
	 *
	 * Various operations are differentiated by their semantics: whether they allow caching, whether they are idempotent and what modifications they allow.
	 */
	enum class Kind {
		/**
		 * Reads some information about a resource.
		 *
		 * This operation cannot lead to any change of state on the whole system.
		 */
		Read,

		/**
		 * Creates a new resource.
		 *
		 * This operation is not idempotent: two [Create] in a row with the same payload will create two different values.
		 */
		Create,

		/**
		 * Edits an existing resource.
		 *
		 * This operation may or may not be idempotent.
		 */
		Edit,

		/**
		 * Deletes an existing resource.
		 */
		Delete,
	}
}
