package opensavvy.backbone

/**
 * The parent interface for an implementation of the Backbone pattern.
 *
 * For more information on the Backbone pattern, please read the module-level documentation.
 *
 * @param Value The object this backbone manages.
 * @param Failure Failures that may be returned when calling [Ref.request].
 * @param Reference The reference responsible for the object [Value].
 */
interface Backbone<Reference : Ref<Failure, Value>, Failure, Value> {

	companion object
}
