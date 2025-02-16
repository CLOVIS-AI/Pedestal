/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.logger

private class LinuxLogger(obj: Any) : Logger {
	override var level: LogLevel = LogLevel.default

	private val objName = obj::class.qualifiedName ?: obj::class.toString()

	private fun buildMessage(vararg values: Any?) =
		values.joinToString(separator = " • ")

	override fun forceTrace(message: String, vararg objects: Any?) {
		println("[TRACE] $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceDebug(message: String, vararg objects: Any?) {
		println("[DEBUG] $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceInfo(message: String, vararg objects: Any?) {
		println("[INFO]  $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceWarn(message: String, vararg objects: Any?) {
		println("[WARN]  $objName: ${buildMessage(message, *objects)}")
	}

	override fun forceError(message: String, vararg objects: Any?) {
		println("[ERROR] $objName: ${buildMessage(message, *objects)}")
	}
}

actual fun loggerFor(obj: Any): Logger =
	LinuxLogger(obj)
