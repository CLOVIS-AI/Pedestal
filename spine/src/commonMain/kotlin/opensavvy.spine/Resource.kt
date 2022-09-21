package opensavvy.spine

import opensavvy.backbone.Data.Companion.markInvalid
import opensavvy.backbone.StateBuilder
import opensavvy.spine.ResourceGroup.AbstractResource

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
	val staticRoutes: Map<Route.Segment, StaticResource<*, *>> get() = _staticRoutes
	private val _staticRoutes: HashMap<Route.Segment, StaticResource<*, *>> = HashMap()

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
	sealed class AbstractResource<O, Context> : ResourceGroup() {

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
		open suspend fun StateBuilder<O>.validateId(id: Id<O>, context: Context) {
			if (id.service != service.name)
				markInvalid(
					ref = null,
					"The passed identifier refers to the service '${id.service}', but this resource belongs to the service '${service.name}'"
				)

			// Let's check that the resource designated by the ID matches with this resource
			var resource: ResourceGroup = this@AbstractResource
			var index = id.resource.segments.lastIndex
			while (resource is AbstractResource<*, *>) {
				val segment = id.resource.segments.getOrNull(index) ?: markInvalid(
					ref = null,
					"The passed identifier's URI length is too short for this resource: $id"
				)

				when (resource) {
					is StaticResource<*, *> -> {
						if (segment != resource.route)
							markInvalid(
								ref = null,
								"The passed identifier's segment #$index doesn't match the resource; expected '${resource.route}' but found '$segment'"
							)
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

			if (index != 0)
				markInvalid(ref = null, "The passed identifier's URI length is too long for this resource: $id")
		}

		@Suppress("LeakingThis") // Not dangerous because Operation's constructor does nothing
		val get = Operation<O, Id<O>, O, Context>(this, Operation.Kind.Read) { it, context -> validateId(it, context) }

		protected fun <In> create(route: Route? = null, validate: OperationValidator<In, O, Context> = { _, _ -> }) =
			Operation(this, Operation.Kind.Create, route, validate)

		protected fun <In> edit(
			route: Route? = null,
			validate: OperationValidator<Pair<Id<O>, In>, Unit, Context> = { _, _ -> },
		) = Operation(this, Operation.Kind.Edit, route) { (id, it): Pair<Id<O>, In>, context ->
			validateId(id, context)
			validate(id to it, context)
		}

		protected fun <In> delete(validate: OperationValidator<Pair<Id<O>, In>, Unit, Context> = { _, _ -> }) =
			Operation(this, Operation.Kind.Delete, route = null) { (id, it): Pair<Id<O>, In>, context ->
				validateId(id, context)
				validate(id to it, context)
			}

	}

	/**
	 * A resource that has a hardcoded ID.
	 *
	 * For example, top-level resources tend to be static: `/users`.
	 * Static resources may also appear as children of other resources: `/users/{id}/emails`.
	 */
	abstract inner class StaticResource<O, Context> protected constructor(route: String) :
		AbstractResource<O, Context>() {

		val route = Route.Segment(route)

		init {
			require(this@ResourceGroup.staticRoutes[this.route] == null) { "A single resource group cannot have multiple static sub resources with the same route ; ${this@ResourceGroup.staticRoutes[this.route]} has been registered before $this" }

			@Suppress("LeakingThis")
			this@ResourceGroup._staticRoutes[this.route] = this
		}

		override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/$route"

		override val parent get() = this@ResourceGroup
		override val service get() = this@ResourceGroup.service
	}

	/**
	 * A template for resources identified by IDs.
	 */
	abstract inner class DynamicResource<O, Context> protected constructor(
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

		override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/{$name}"

		override val parent get() = this@ResourceGroup
		override val service get() = this@ResourceGroup.service
	}
}
