package opensavvy.spine

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = Id.Serializer::class)
data class Id(
	val service: Route.Segment,
	val resource: Route,
) {

	constructor(service: String, resource: Route) : this(Route.Segment(service), resource)

	override fun toString() = "$service/$resource"

	//region Serializer
	internal object Serializer : KSerializer<Id> {
		override val descriptor = PrimitiveSerialDescriptor("opensavvy.spine.Id", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: Id) {
			encoder.encodeString("${value.service}/${value.resource}")
		}

		override fun deserialize(decoder: Decoder): Id {
			val segments = decoder.decodeString().split('/')
				.map { Route.Segment(it) }

			return Id(segments.first(), Route(segments.drop(1)))
		}
	}
	//endregion
}

@Serializable
data class Identified<T>(
	val id: Id,
	val value: T,
)
