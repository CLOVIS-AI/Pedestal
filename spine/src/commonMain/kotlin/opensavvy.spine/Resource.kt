package opensavvy.spine

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
	val staticRoutes: Map<Route.Segment, StaticResource<*>> get() = _staticRoutes
	private val _staticRoutes: HashMap<Route.Segment, StaticResource<*>> = HashMap()

	/**
	 * The [dynamic resource][DynamicResource] that appears as a direct child of this resource group.
	 *
	 * If no dynamic resource were registered, this property is `null`.
	 */
	var dynamicRoute: DynamicResource<*>? = null
		private set

	/**
	 * All resources that appear as direct children of this resource group (sum of [staticRoutes] and [dynamicRoute]).
	 *
	 * To access all routes including non-direct children of this resource group, see [routesRecursively].
	 */
	val routes: Sequence<AbstractResource>
		get() = sequence {
			yieldAll(staticRoutes.values)
			dynamicRoute?.let { yield(it) }
		}

	/**
	 * All resources that appear as children of this resource group, directly or non-directly.
	 *
	 * To access only direct children, see [routes].
	 */
	val routesRecursively: Sequence<AbstractResource>
		get() = routes.flatMap { sequenceOf(it) + it.routesRecursively }

	/**
	 * A pseudo-URI representing this resource.
	 *
	 * The value returned by this method is not a valid URI: when dynamic resources are declared, each concrete resource would add its ID in the URI.
	 */
	abstract val routeTemplate: String

	override fun toString() = routeTemplate

	/**
	 * Common ancestor of [StaticResource] and [DynamicResource].
	 *
	 * End users should not use this class directly.
	 */
	sealed class AbstractResource : ResourceGroup()

	/**
	 * A resource that has a hardcoded ID.
	 *
	 * For example, top-level resources tend to be static: `/users`.
	 * Static resources may also appear as children of other resources: `/users/{id}/emails`.
	 */
	abstract inner class StaticResource<O> protected constructor(route: String) : AbstractResource() {

		val route = Route.Segment(route)

		init {
			require(this@ResourceGroup.staticRoutes[this.route] == null) { "A single resource group cannot have multiple static sub resources with the same route ; ${this@ResourceGroup.staticRoutes[this.route]} has been registered before $this" }

			@Suppress("LeakingThis")
			this@ResourceGroup._staticRoutes[this.route] = this
		}

		override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/$route"
	}

	/**
	 * A template for resources identified by IDs.
	 */
	abstract inner class DynamicResource<O> protected constructor(
		/**
		 * The name of the identifier.
		 *
		 * When a request is made to this resource, the ID appears as a parameter under this name.
		 */
		val name: String
	) : AbstractResource() {

		init {
			require(this@ResourceGroup.dynamicRoute == null) { "A single resource group cannot have multiple dynamic sub resources ; ${this@ResourceGroup.dynamicRoute} has been registered before $this" }

			@Suppress("LeakingThis")
			this@ResourceGroup.dynamicRoute = this
		}

		override val routeTemplate get() = "${this@ResourceGroup.routeTemplate}/{$name}"
	}
}
