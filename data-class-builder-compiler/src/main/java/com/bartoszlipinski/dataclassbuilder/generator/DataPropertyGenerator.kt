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
package com.bartoszlipinski.dataclassbuilder.generator

import com.bartoszlipinski.dataclassbuilder.BuilderCandidate
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.MEMBER_NAME_CHECK_CONTAINS_FLAG
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.MEMBER_NAME_CHECK_NOT_NULL
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.PRIVATE_PROPERTY_PREFIX
import com.bartoszlipinski.dataclassbuilder.internal.asIterable
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

internal interface DataPropertyGenerationStrategy {

    /**
     * This returns `Iterable` because some strategies can create more than one
     * internal property.
     */
    fun generate(property: BuilderCandidate.Property): Iterable<PropertySpec>
}

@Suppress("ClassName")
object DataPropertyGenerator : DataPropertyGenerationStrategy {

    fun generateSignature(property: BuilderCandidate.Property): PropertySpec {
        return PropertySpec
                .builder(property.name, property.type())
                .mutable()
                .addModifiers(KModifier.ABSTRACT)
                .build()
    }

    override fun generate(property: BuilderCandidate.Property): Iterable<PropertySpec> {
        return strategyFor(property).generate(property)
    }

    private fun strategyFor(property: BuilderCandidate.Property) =
            if (property.isNullable && property.hasDefaultValue) {
                `exposed var WITH changelog in the setter`
            } else if (property.hasChangelog()) {
                `private var WITH changelog in the setter`
            } else {
                `private var WITHOUT changelog in the setter`
            }

    /**
     * Simple nullable variable that implements the DSL property.
     * Setter modifies the changelog property.
     *
     *   override var a: String? = null
     *     set(value) {
     *       field = value
     *       changelog = changelog or 0b010
     *     }
     */
    object `exposed var WITH changelog in the setter` : DataPropertyGenerationStrategy {

        override fun generate(property: BuilderCandidate.Property): Iterable<PropertySpec> {
            require(property.hasDefaultValue)

            val type = property.type()
            return PropertySpec
                    .builder(property.name, type)
                    .mutable()
                    .addModifiers(KModifier.OVERRIDE)
                    .initializer("null")
                    .setter(FunSpec
                            .setterBuilder()
                            .addParameter("value", type)
                            .addStatement("field = value")
                            .addStatement("%L = %L or %L",
                                    KotlinFileGenerator.PROPERTY_CHANGELOG,
                                    KotlinFileGenerator.PROPERTY_CHANGELOG,
                                    property.changelogValueBinary
                            )
                            .build())
                    .build()
                    .asIterable()
        }
    }

    /**
     * Generates a pair of variables that act similarly to `lateinit var`.
     * This was used instead of using `lateinit var` for two reasons:
     * 1. `lateinit var` cannot be used for primitives
     * 2. We want to control what kind of exception is thrown when this wasn't set and `build()` was used.
     *    Using `lateinit var` would expose the internals of the `Builder` with the type of Exception being thrown.
     *
     * If the internal var is null, it means the property was not set.
     *
     *   private var privateA: String? = null
     *   var a : String
     *     get() = checkNotNull(privateA) { "<ERROR_MSG>" }
     *     set(value) {
     *       privateA = value
     *     }
     */
    object `private var WITHOUT changelog in the setter` : DataPropertyGenerationStrategy {
        override fun generate(property: BuilderCandidate.Property): Iterable<PropertySpec> {
            val privatePropertyName = property.privatePropertyName()
            val privateProperty = PropertySpec
                    .builder(privatePropertyName, property.asNullableType())
                    .mutable()
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("null")
                    .build()

            val exposedGetter = FunSpec
                    .getterBuilder()
                    .addStatement(
                            "return %M(%L) { %S }",
                            MEMBER_NAME_CHECK_NOT_NULL,
                            privatePropertyName,
                            errorMsg(property)
                    )
                    .build()
            val exposedSetter = FunSpec
                    .setterBuilder()
                    .addParameter("value", property.type())
                    .addStatement("%L = value", privatePropertyName)
                    .build()

            val exposedProperty = PropertySpec
                    .builder(property.name, property.type())
                    .mutable()
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(exposedGetter)
                    .setter(exposedSetter)
                    .build()

            return listOf(privateProperty, exposedProperty)
        }
    }

    /**
     * Generates a pair of variables that act similarly to `lateinit var`.
     * This was used instead of using `lateinit var` for two reasons:
     * 1. `lateinit var` cannot be used for primitives
     * 2. We want to control what kind of exception is thrown when this wasn't set and `build()` was used.
     *    Using `lateinit var` would expose the internals of the `Builder` with the type of Exception being thrown.
     *
     *  If the changelog was not recorded, it means the property was not set.
     *
     *   private var privateA: String? = null
     *   var a : String
     *      get() {
     *        checkContainsFlag(changelog, 0b100) { "<ERROR_MSG>" }
     *        return privateC
     *      }
     *     set(value) {
     *       privateA = value
     *       changelog = changelog or 0b100
     *     }
     */
    object `private var WITH changelog in the setter` : DataPropertyGenerationStrategy {

        override fun generate(property: BuilderCandidate.Property): Iterable<PropertySpec> {
            val privatePropertyName = property.privatePropertyName()
            val privateProperty = PropertySpec.builder(privatePropertyName, property.asNullableType())
                    .mutable()
                    .addModifiers(KModifier.PRIVATE)
                    .initializer("null")
                    .build()

            val exposedGetter = FunSpec
                    .getterBuilder()
                    .addStatement(
                            "%M(%L, %L) { %S }",
                            MEMBER_NAME_CHECK_CONTAINS_FLAG,
                            KotlinFileGenerator.PROPERTY_CHANGELOG,
                            property.changelogValueBinary,
                            errorMsg(property)
                    )
            if (property.isNullable) {
                exposedGetter.addStatement("return %L", privatePropertyName)
            } else {
                exposedGetter.addStatement("return %L!!", privatePropertyName) //force-nullability
            }

            val exposedSetter = FunSpec
                    .setterBuilder()
                    .addParameter("value", property.type())
                    .addStatement("%L = value", privatePropertyName)
                    .addStatement("%L = %L or %L",
                            KotlinFileGenerator.PROPERTY_CHANGELOG,
                            KotlinFileGenerator.PROPERTY_CHANGELOG,
                            property.changelogValueBinary
                    )
                    .build()

            val exposedProperty = PropertySpec
                    .builder(property.name, property.type())
                    .mutable()
                    .addModifiers(KModifier.OVERRIDE)
                    .getter(exposedGetter.build())
                    .setter(exposedSetter)
                    .build()

            return listOf(privateProperty, exposedProperty)
        }
    }

    private fun BuilderCandidate.Property.privatePropertyName(): String {
        return PRIVATE_PROPERTY_PREFIX + this.name.capitalize()
    }

    private fun errorMsg(property: BuilderCandidate.Property): String {
        return "Required parameter missing: '${property.name}'"
    }
}