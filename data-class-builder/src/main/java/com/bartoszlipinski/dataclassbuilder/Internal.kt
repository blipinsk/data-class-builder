/*
 * Copyright 2020 Bartosz Lipinski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bartoszlipinski.dataclassbuilder

inline fun <T : Any> checkNotNull(value: T?, lazyMessage: () -> String): T {
    if (value == null) {
        val message = lazyMessage()
        throw DataClassBuilderException(message)
    } else {
        return value
    }
}

inline infix fun Int.containsFlag(flag: Int): Boolean {
    return this and flag == flag
}

inline fun checkContainsFlag(changelog: Int, flag: Int, lazyMessage: () -> String) {
    if (!changelog.containsFlag(flag)) {
        val message = lazyMessage()
        throw DataClassBuilderException(message)
    }
}