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
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.CLASS_NAME_INT
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.CLASS_NAME_LONG
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.PROPERTY_CHANGELOG
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.STATEMENT_RETURN_THIS
import com.bartoszlipinski.dataclassbuilder.internal.asBinaryString
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec

object BuilderGenerator {

    fun generateConstructor() = FunSpec
            .constructorBuilder()
            .addModifiers(KModifier.INTERNAL)
            .build()

    fun generateChangelogProperty(candidate: BuilderCandidate): PropertySpec? {
        //if has any properties with default value -> generating changelog property
        val count = candidate.propertiesWithChangelog
        if (count > 0) {
            val intFlagCount = Int.SIZE_BITS - 1 //-1 is because Ints are signed

            val baseChangelog = 0.asBinaryString(candidate.propertiesWithChangelog)
            val type = if (count > intFlagCount) {
                CLASS_NAME_LONG
            } else {
                CLASS_NAME_INT
            }
            return PropertySpec.builder(PROPERTY_CHANGELOG, type)
                    .mutable()
                    .addModifiers(KModifier.PRIVATE)
                    .initializer(baseChangelog)
                    .build()
        }
        return null
    }

    fun generateDataPropertySignatures(candidate: BuilderCandidate): Iterable<PropertySpec> {
        return candidate
                .properties
                .map { property -> DataPropertyGenerator.generateSignature(property) }
    }

    fun generateDataProperties(candidate: BuilderCandidate): Iterable<PropertySpec> {
        return candidate
                .properties
                .flatMap { property -> DataPropertyGenerator.generate(property) }
    }

    fun generateDataFunctionSignatures(candidate: BuilderCandidate): Iterable<FunSpec> =
            candidate
                    .properties
                    .map { property ->
                        FunSpec.builder(property.name)
                                .returns(candidate.builderInterfaceName)
                                .addModifiers(KModifier.ABSTRACT)
                                .addParameter(property.name, property.type())
                                .build()
                    }

    fun generateDataFunctions(candidate: BuilderCandidate): Iterable<FunSpec> =
            candidate
                    .properties
                    .map { property ->
                        val builder = FunSpec.builder(property.name)
                                .returns(candidate.builderInterfaceName)
                                .addModifiers(KModifier.OVERRIDE)
                                .addParameter(property.name, property.type())
                                .addStatement("this.%L = %L", property.name, property.name)

                        builder.addStatement(STATEMENT_RETURN_THIS).build()
                    }

    fun generateBuildFun(candidate: BuilderCandidate) = BuildFunGenerator.generate(candidate)
}