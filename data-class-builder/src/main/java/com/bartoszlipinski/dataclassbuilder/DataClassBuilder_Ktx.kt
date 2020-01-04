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
@file:Suppress("UNUSED_PARAMETER", "unused")

package com.bartoszlipinski.dataclassbuilder

import kotlin.reflect.KClass

interface Builder<T : Any> {
    fun build(): T
}

interface BuilderDsl<T : Any> {
    //empty interface
}

@Suppress("unused") //<-- unused Receiver Type (we need it to be extension fun for proper method resolution)
fun <T : Any> Any.dataClassBuilder(kClass: KClass<T>): Builder<T> {
    return object : Builder<T> {
        override fun build(): T {
            error("Builder was not generated for $kClass")
        }
    }
}

//this is meant for the companion object
@Suppress("unused") //<-- unused Receiver Type (we need it to be extension fun for proper method resolution)
fun <T : Any> T.dataClassBuilder(): Builder<T> {
    return object : Builder<T> {
        override fun build(): T {
            error("Builder for ${this::class}: either the annotation is missing or the class does not have a companion object defined")
        }
    }
}

@Suppress("unused") //<-- unused Receiver Type (we need it to be extension fun for proper method resolution)
fun <T : Any> Any.buildDataClass(@Suppress("UNUSED_PARAMETER") kClass: KClass<T>, func: BuilderDsl<T>.() -> Unit): T {
    error("Builder DSL was not generated for $kClass")
}

//this is meant for the companion object
fun <T : Any> T.buildDataClass(func: BuilderDsl<T>.() -> Unit): T {
    error("Builder for ${this::class}: either the annotation is missing or the class does not have a companion object defined")
}