package opensavvy.spine

import arrow.core.raise.Raise
import opensavvy.state.arrow.out
import kotlin.js.JsName

typealias OperationValidator<In, Params, Failure, Context> = suspend Operation.ValidatorScope<In, Params, Failure, Context>.() -> Unit

class Operation<Resource : Any, In : Any, Failure : Any, Out : Any, Params : Parameters, Context : Any>(
	val resource: ResourceGroup.AbstractResource<Resource, Context>,
	val kind: Kind,
	val route: Route? = null,
	@JsName("_validate") private val validate: OperationValidator<In, Params, Failure, Context>,
) {

	/**
	 * Instantiates an [Id] for the resource this operation is based on.
	 *
	 * This is simply syntax sugar for calling [resource].[idOf][ResourceGroup.AbstractResource.idOf].
	 */
	fun idOf(vararg dynamic: String) = resource.idOf(*dynamic)

	suspend fun validate(id: Id, body: In, parameters: Params, context: Context) = out<SpineFailure<Failure>, Unit> {
		val scope = ValidatorScope(this, id, body, parameters, context)
		scope.validate()
	}

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
		 * Executes an arbitrary action on a resource.
		 *
		 * This operation should not be confused with [Create], [Edit] and [Delete].
		 *
		 * This operation may or may not be idempotent.
		 */
		Action,

		/**
		 * Deletes an existing resource.
		 */
		Delete,
	}

	class ValidatorScope<In : Any, Params : Parameters, Failure : Any, Context> internal constructor(
		private val scope: Raise<SpineFailure<Failure>>,

		val id: Id,

		val body: In,

		val parameters: Params,

		val context: Context,
	) : Raise<SpineFailure<Failure>> by scope
}
