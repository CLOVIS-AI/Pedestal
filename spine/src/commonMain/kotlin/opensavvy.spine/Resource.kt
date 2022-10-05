package opensavvy.spine

import opensavvy.spine.ResourceGroup.AbstractResource
import opensavvy.spine.Route.Companion.div
import opensavvy.state.StateBuilder
import opensavvy.state.ensureValid

/**
 * Common ancestor of [Service] and [AbstractResource].
 *
 * This class is used to declare common behavior between services and resources.
 * End users should not implement directly.
 */
sealed class ResourceGroup {

	/**
	 * All [static resources][StaticResource] that appear as direct children of this resource group.
	 *
	 * If no static resources were registered, this collection is empty.
	 */
	val staticRoutes: Map<Route.Segment, StaticResource<*, *, *>> get() = _staticRoutes
	private val _staticRoutes: HashMap<Route.Segment, StaticResource<*, *, *>> = HashMap()

	/**
	 * The [dynamic resource][DynamicResource] that appears as a direct child of this resource group.
	 *
	 * If no dynamic resource were registered, this property is `null`.
	 */
	var dynamicRoute: DynamicResource<*, *>? = null
		private set

	/**
	 * All resources that appear as direct children of this resource group (sum of [staticRoutes] and [dynamicRoute]).
	 *
	 * To access all routes including non-direct children of this resource group, see [routesRecursively].
	 */
	val routes: Sequence<AbstractResource<*, *>>
		get() = sequence {
			yieldAll(staticRoutes.values)
			dynamicRoute?.let { yield(it) }
		}

	/**
	 * All resources that appear as children of this resource group, directly or non-directly.
	 *
	 * To access only direct children, see [routes].
	 */
	val routesRecursively: Sequence<AbstractResource<*, *>>
		get() = routes.flatMap { sequenceOf(it) + it.routesRecursively }

	/**
	 * A pseudo-URI representing this resource.
	 *
	 * The value returned by this method is not a valid URI: when dynamic resources are declared, each concrete resource would add its ID in the URI.
	 */
	abstract val routeTemplate: String

	/**
	 * Retrieves the [Service] which is responsible for this [ResourceGroup].
	 */
	abstract val service: Service

	override fun toString() = routeTemplate

	/**
	 * Common ancestor of [StaticResource] and [DynamicResource].
	 *
	 * End users should not use this class directly.
	 */
	sealed class AbstractResource<O : Any, Context : Any> : ResourceGroup() {

		/**
		 * The direct parent of this resource in the URI hierarchy.
		 */
		abstract val parent: ResourceGroup

		/**
		 * Verifies that [id] is a valid identifier for this resource.
		 *
		 * This function is automatically called to verify all identifiers passed to all endpoints to this resource ([get] and the other functions).
		 * It can be overridden to add checks for user rights, etc.
		 */
		open suspend fun StateBuilder<Id<O>, O>.validateId(id: Id<O>, context: Context) {
			ensureValid(
				id,
				id.service == service.name
			) { "The passed identifier refers to the service '${id.service}', but this resource belongs to the service '${service.name}'" }

			// Let's check that the resource designated by the ID matches with this resource
			var resource: ResourceGroup = this@AbstractResource
			var index = id.resource.segments.lastIndex
			while (resource is AbstractResource<*, *>) {
				val segment = id.resource.segments.getOrNull(index)
				ensureValid(
					id,
					segment != null
				) { "The passed identifier's URI length is too short for this resource: '$id' for resource '${this@AbstractResource}'" }

				@Suppress("NAME_SHADOWING") // necessary for smart cast because 'resource' is mutable
				when (val resource: AbstractResource<*, *> = resource) {
					is StaticResource<*, *, *> -> {
						ensureValid(
							id,
							segment == resource.route
						) { "The passed identifier's segment #$index doesn't match the resource; expected '${resource.route}' but found '$segment'" }
					}

					is DynamicResource<*, *> -> {
						// There are no constraints on what IDs look like.
						// If we expect an ID, we can't make any verification on the value.
					}

					// else -> is impossible
				}

				resource = resource.parent
				index--
			}

			ensureValid(
				id,
				index == -1
			) { "The passed identifier's URI length is too long for this resource: '$id' for resource '${this@AbstractResource}'" }
		}

		protected fun <In : Any, Out : Any, Params : Parameters> create(
			route: Route? = null,
			validate: OperationValidator<In, Out, Params, Context> = { _, _, _ -> },
		) =
			Operation(this, Operation.Kind.Create, route, validate)

		protected fun <In, Params : Parameters> edit(
			route: Route? = null,
			validate: OperationValidator<Pair<Id<O>, In>, Unit, Params, Context> = { _, _, _ -> },
		) = Operation(this, Operation.Kind.Edit, route) { (id, it): Pair<Id<O>, In>, params: Params, context ->
			validateId(id, context)
			validate(id to it, params, context)
		}

		protected fun <In> delete(validate: OperationValidator<Pair<Id<O>, In>, Unit, Parameters.Empty, Context> = { _, _, _ -> }) =
			Operation(
				this,
				Operation.Kind.Delete,
				route = null
			) { (id, it): Pair<Id<O>, In>, _: Parameters.Empty, context ->
				validateId(id, context)
				validate(id to it, Parameters.Empty, context)
			}

		/**
		 * Instantiates an [Id] for this resource.
		 *
		 * Because this corresponds to instantiating a [Route], it is necessary to replace all dynamic identifiers with
		 * their concrete values.
		 *
		 * The user is responsible for providing the [dynamic] values.
		 *
		 * Here are a few examples of proper usage:
		 * - for resource `/users`: `idOf()` generates the ID `/users`
		 * - for resource `/users/{user}`: `idOf("52f8")` generates the ID `/users/52f8`
		 * - for resource `/users/{user}/{pet}`: `idOf("52f8", "a32b")` generates the ID `/users/52f8/a32b`
		 */
		fun idOf(vararg dynamic: String): Id<O> = idOf(dynamic.asSequence().map { Route.Segment(it) }.iterator())

		/**
		 * Instantiates an [Id] for this resource.
		 *
		 * This method has a lower level interface, we recommend using the [idOf] overload with a vararg instead.
		 *
		 * Implementations of this method should consume the number of dynamic elements they need from the [dynamic] iterator
		 * and ignore any other value (calling this function with an iterator that has too many elements is correct).
		 */
		abstract fun idOf(dynamic: Iterator<Route.Segment>): Id<O>
	}

	/**
	 * A resource that has a hardcoded ID.
	 *
	 * For example, top-level resources tend to be static: `/users`.
	 * Static resources may also appear as children of other resources: `/users/{id}/emails`.
	 */
	abstract inner class StaticResource<O : Any, GetParams : Parameters, Context : Any> protected constructor(route: String) :
		AbstractResource<O, Context>() {

		val route = Route.Segment(route)

		init {
			require(this@ResourceGroup.staticRoutes[this.route] == null) { "A single resource group cannot have multiple static sub resources with the same route ; ${this@ResourceGroup.staticRoutes[this.route]} has been registered before $this" }

			@Suppress("LeakingThis")
			this@ResourceGroup._staticRoutes[this.route] = this
		}

		/**
		 * Validates that [params] allow the user to access this resource.
		 *
		 * You should override this function if the parameters impact the access rights.
		 */
		open suspend fun StateBuilder<Id<O>, O>.validateGetParams(params: GetParams, context: Context) {}

		@Suppress("LeakingThis") // Not dangerous because Operation's constructor does nothing
		val get = Operation<O, Id<O>, O, GetParams, Context>(this, Operation.Kind.Read) { it, params, context ->
			validateId(it, context)
			validateGetParams(params, context)
		}

		final override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/$route"

		final override val parent get() = this@ResourceGroup
		final override val service get() = this@ResourceGroup.service

		final override fun idOf(dynamic: Iterator<Route.Segment>): Id<O> {
			val parentId = when (val parent = parent) {
				is AbstractResource<*, *> -> parent.idOf(dynamic)
				is Service -> parent.idOf()
			}

			return Id(parentId.service, parentId.resource / route)
		}
	}

	/**
	 * A template for resources identified by IDs.
	 */
	abstract inner class DynamicResource<O : Any, Context : Any> protected constructor(
		/**
		 * The name of the identifier.
		 *
		 * When a request is made to this resource, the ID appears as a parameter under this name.
		 */
		val name: String,
	) : AbstractResource<O, Context>() {

		init {
			require(this@ResourceGroup.dynamicRoute == null) { "A single resource group cannot have multiple dynamic sub resources ; ${this@ResourceGroup.dynamicRoute} has been registered before $this" }

			@Suppress("LeakingThis")
			this@ResourceGroup.dynamicRoute = this
		}

		@Suppress("LeakingThis") // Not dangerous because Operation's constructor does nothing
		val get = Operation<O, Id<O>, O, Parameters.Empty, Context>(this, Operation.Kind.Read) { it, _, context ->
			validateId(
				it,
				context
			)
		}

		final override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/{$name}"

		final override val parent get() = this@ResourceGroup
		final override val service get() = this@ResourceGroup.service

		final override fun idOf(dynamic: Iterator<Route.Segment>): Id<O> {
			val parentId = when (val parent = parent) {
				is AbstractResource<*, *> -> parent.idOf(dynamic)
				is Service -> parent.idOf()
			}

			check(dynamic.hasNext()) { "Not enough dynamic elements were passed to the idOf function of resource '$this'; stuck after '${parentId.resource}'" }
			val route = dynamic.next()

			return Id(parentId.service, parentId.resource / route)
		}
	}
}
