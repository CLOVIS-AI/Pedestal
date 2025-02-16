/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.backbone

import arrow.core.raise.ensure
import opensavvy.cache.cache
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.state.arrow.out
import opensavvy.state.outcome.valueOrNull

private data class BasicRef(val id: String, val backbone: Bone) : Ref<Bone.Invalid, Int> {

	override fun request() = backbone.cache[this]
}

// Id("12") -> 12
private class Bone : Backbone<BasicRef, Bone.Invalid, Int> {
	val cache = cache<BasicRef, Invalid, Int> {
		out {
			val int = it.id.toIntOrNull()
			ensure(int != null) { Invalid }
			int
		}
	}

	fun of(int: Int) = BasicRef(int.toString(), this)

	object Invalid
}

class BackboneCacheTest : PreparedSpec({

	test("Default") {
		val bone = Bone()
		val id5 = bone.of(5)
		val id2 = bone.of(2)

		check(id5.now().valueOrNull == 5)
		check(id2.now().valueOrNull == 2)

		bone.cache.expire(id2)
		check(id2.now().valueOrNull == 2)
	}

	test("Existence of companions") {
		println(Ref)
		println(Backbone)
	}
})
