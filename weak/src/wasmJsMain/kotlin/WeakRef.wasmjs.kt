/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

package opensavvy.pedestal.weak

import opensavvy.pedestal.weak.algorithms.EmptyWeakRef

@JsName("WeakRef")
private external class JsWeakRef {
	constructor(target: JsAny)

	fun deref(): JsAny?
}

private class WrappedWeakRef<T : Any>(
	target: T,
) : WeakRef<T> {
	val js = JsWeakRef(target.toJsReference())

	override fun read(): T? =
		js.deref()?.unsafeCast<JsReference<T>>()?.get()

	override fun toString() =
		js.toString()
}

/**
 * Implementation of [WeakRef] backed by a JS [WeakRef](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakRef).
 *
 * JS doesn't make a difference between weak and soft references.
 */
actual fun <T> WeakRef(value: T): WeakRef<T> =
	if (value == null) EmptyWeakRef()
	else WrappedWeakRef(value)

/**
 * Implementation of [WeakRef] backed by a JS [WeakRef](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/WeakRef).
 *
 * JS doesn't make a difference between weak and soft references.
 */
@Suppress("FunctionName")
actual fun <T> SoftRef(value: T): WeakRef<T> =
	WeakRef(value)
