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

import kotlinx.cinterop.ptr
import platform.darwin.*

class IosLogger(self: Any): Logger {
    override var level = LogLevel.default

    private val tag = self::class.simpleName

    override fun forceTrace(message: String, vararg objects: Any?) {
        val string = "$tag: $message ${objects.joinToString(" ")}"
        _os_log_internal(__dso_handle.ptr, OS_LOG_DEFAULT, OS_LOG_TYPE_DEFAULT, "%s", string)
    }

    override fun forceDebug(message: String, vararg objects: Any?) {
        val string = "$tag: $message ${objects.joinToString(" ")}"
        _os_log_internal(__dso_handle.ptr, OS_LOG_DEFAULT, OS_LOG_TYPE_DEBUG, "%s", string)
    }

    override fun forceInfo(message: String, vararg objects: Any?) {
        val string = "$tag: $message ${objects.joinToString(" ")}"
        _os_log_internal(__dso_handle.ptr, OS_LOG_DEFAULT, OS_LOG_TYPE_INFO, "%s", string)
    }

    override fun forceWarn(message: String, vararg objects: Any?) {
        val string = "$tag: $message ${objects.joinToString(" ")}"
        _os_log_internal(__dso_handle.ptr, OS_LOG_DEFAULT, OS_LOG_TYPE_DEFAULT, "%s", string)
    }

    override fun forceError(message: String, vararg objects: Any?) {
        val string = "$tag: $message ${objects.joinToString(" ")}"
        _os_log_internal(__dso_handle.ptr, OS_LOG_DEFAULT, OS_LOG_TYPE_ERROR, "%s", string)
    }
}

actual fun loggerFor(obj: Any): Logger = IosLogger(obj)
