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
package com.bartoszlipinski.dataclassbuilder.internal

fun <T : Any> T.onTrue(condition: Boolean, executable: T.() -> Unit): T {
    if (condition) this.apply(executable)
    return this
}

fun Int.asBinaryString(resolution: Int): String {
    val binaryString = this.toString(radix = 2)
    val paddedString = binaryString.padStart(resolution, '0')
    return "0b$paddedString"
}

fun Long.asBinaryString(resolution: Int): String {
    Long.SIZE_BITS
    val binaryString = this.toString(radix = 2)
    val paddedString = binaryString.padStart(resolution, '0')
    return "0b$paddedString"
}

//TODO optimize not to create the list
fun <T : Any> T.asIterable(): Iterable<T> = listOf(this)