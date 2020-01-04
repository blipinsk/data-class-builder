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
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.MEMBER_NAME_CONTAINS_FLAG
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.PROPERTY_CHANGELOG
import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator.Companion.VARIABLE_INSTANCE
import com.bartoszlipinski.dataclassbuilder.internal.asBinaryString
import com.bartoszlipinski.dataclassbuilder.internal.compilerWarning
import com.bartoszlipinski.dataclassbuilder.internal.onTrue
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier

object BuildFunGenerator {

    fun generate(candidate: BuilderCandidate): FunSpec {
        return GenerationStrategy.strategyFor(candidate).generate(candidate)
    }

    private fun createFunBuilder(candidate: BuilderCandidate): FunSpec.Builder {
        return FunSpec
                .builder(KotlinFileGenerator.FUN_BUILD)
                .returns(candidate.sourceClassName)
                .addModifiers(KModifier.OVERRIDE)
    }

    private fun CodeBlock.Builder.addConstructorExecution(
            dataClassName: ClassName,
            properties: List<BuilderCandidate.Property>
    ) = apply {
        add("%T(", dataClassName)
        properties.forEachIndexed { index, property ->
            onTrue(index != 0) { add(", ") }
            add("%L = this.%L", property.name, property.name)
        }
        add(")")
        return this
    }

    //TODO optimize
    private fun generatePropertyCombinations(
            candidate: BuilderCandidate
    ): Map<Long, List<BuilderCandidate.Property>> {
        val resultMap = mutableMapOf<Long, List<BuilderCandidate.Property>>()
        generatePropertyCombination(
                0,
                0,
                listOf(),
                candidate.properties,
                resultMap
        )
        return resultMap
    }

    //TODO optimize
    private fun generatePropertyCombination(
            currentIndex: Int,
            chosenChangelog: Long,
            chosenProperties: List<BuilderCandidate.Property>,
            allProperties: List<BuilderCandidate.Property>,
            resultMap: MutableMap<Long, List<BuilderCandidate.Property>>
    ) {
        if (currentIndex == allProperties.size) {
            resultMap[chosenChangelog] = chosenProperties
            return
        }

        val currentProperty = allProperties[currentIndex]
        val newChosenProperties = chosenProperties.toMutableList()
        newChosenProperties.add(currentProperty)

        if (currentProperty.hasDefaultValue) {
            generatePropertyCombination(
                    currentIndex + 1,
                    chosenChangelog or currentProperty.changelogValue,
                    newChosenProperties,
                    allProperties,
                    resultMap
            )
            generatePropertyCombination(
                    currentIndex + 1,
                    chosenChangelog,
                    chosenProperties,
                    allProperties,
                    resultMap
            )
        } else {
            generatePropertyCombination(
                    currentIndex + 1,
                    chosenChangelog,
                    newChosenProperties,
                    allProperties,
                    resultMap
            )
        }
    }

    interface GenerationStrategy {

        fun generate(candidate: BuilderCandidate): FunSpec

        companion object {
            // this is an arbitrary value, no deep thinking went into choosing it
            // it was just eye-balled (based on the exponential generation time)
            private const val MAX_PROPERTIES_WITH_DEFAULT_VALUES = 5

            fun strategyFor(candidate: BuilderCandidate): GenerationStrategy {
                val defaultValues = candidate.propertiesWithDefaultValue.size
                return when {
                    defaultValues > MAX_PROPERTIES_WITH_DEFAULT_VALUES -> {
                        compilerWarning("[${candidate.sourceClassName.canonicalName}] " +
                                "has $defaultValues properties with default values. " +
                                "Reduce the number of them " +
                                "(to $MAX_PROPERTIES_WITH_DEFAULT_VALUES or less) " +
                                "for the generated code to be better in terms of performance."
                        )
                        CopyConstructorStrategy
                    }
                    defaultValues > 0 -> WhenTreeStrategy
                    else -> PrimaryConstructorStrategy
                }
            }
        }
    }

    /**
     * Generates the simplest `fun build()` possible. Just invoking the primary constructor.
     */
    object PrimaryConstructorStrategy : GenerationStrategy {
        override fun generate(candidate: BuilderCandidate): FunSpec {
            val buildFun = createFunBuilder(candidate)
            val buildFunCode = CodeBlock.Builder()
                    .addLine("return ")
                    .indent()
                    .addConstructorExecution(candidate.sourceClassName, candidate.properties)
                    .unindent()

            return buildFun
                    .addCode(buildFunCode.build())
                    .build()
        }
    }

    /**
     * Generates `fun build()` based on invoking a `copy()` fun.
     * It allows us to use default values of properties, but can be costly in terms of creating
     * the data class instance:
     * 1. Creates two instances of the data class every single time.
     * 2. Initializes all default values (those might be complex expressions) even when they are not used.
     */
    object CopyConstructorStrategy : GenerationStrategy {
        override fun generate(candidate: BuilderCandidate): FunSpec {
            val buildFun = createFunBuilder(candidate)

            // generating constructor invocation without using any of the properties with default values
            // (this will initilize all default values)
            val buildFunCode = CodeBlock.Builder()
                    .add("var %L = ", VARIABLE_INSTANCE)
                    .addConstructorExecution(candidate.sourceClassName, candidate.propertiesWithoutDefault)
                    .addLine()
                    .addLine("return %L.copy(", VARIABLE_INSTANCE)
                    .indent()

            candidate.propertiesWithDefaultValue.forEachIndexed { index, property ->
                val changelogValue = property.changelogValue.asBinaryString(candidate.propertiesWithChangelog)

                buildFunCode.onTrue(index != 0) { add(",\n") }
                buildFunCode.add("%L = if(%L %M %L) this.%L", property.name, PROPERTY_CHANGELOG, MEMBER_NAME_CONTAINS_FLAG, changelogValue, property.name)
                buildFunCode.add(" else %L.%L", VARIABLE_INSTANCE, property.name)
            }

            buildFunCode
                    .unindent()
                    .addLine()
                    .addLine(")")

            return buildFun
                    .addCode(buildFunCode.build())
                    .build()
        }
    }

    /**
     * Generates the most complex version of `fun build()`. This will have a `when`-based tree,
     * with different constructor invocations depending on what was used on the builder.
     * Generation is costly, therefore we cannot use it when there are too many properties
     * with default values defined in the `data class`.
     */
    object WhenTreeStrategy : GenerationStrategy {
        override fun generate(candidate: BuilderCandidate): FunSpec {

            val buildFun = createFunBuilder(candidate)
            val buildFunBuilder = CodeBlock.Builder()

            var errorMsg = "Illegal `$PROPERTY_CHANGELOG` value=\$${PROPERTY_CHANGELOG}"
            if (candidate.changelogTracksAnyNonDefaults()) {
                val defaultsMask = candidate
                        .changelogTrackingDefaults()
                        .asBinaryString(candidate.propertiesWithChangelog)
                errorMsg += " defaults=${defaultsMask}"
                buildFunBuilder.addLine("return when (%L and %L) {", PROPERTY_CHANGELOG, defaultsMask)
            } else {
                buildFunBuilder.addLine("return when (%L) {", PROPERTY_CHANGELOG)
            }

            buildFunBuilder.indent()

            val combinations = generatePropertyCombinations(candidate)
            combinations.forEach { entry ->
                buildFunBuilder
                        .add("%L -> ", entry.key.asBinaryString(candidate.propertiesWithChangelog))
                        .addConstructorExecution(candidate.sourceClassName, entry.value)
                        .addLine()
            }

            buildFunBuilder
                    .addLine("else -> error(%P)", errorMsg)
                    .unindent()
                    .addLine("}")
            return buildFun
                    .addCode(buildFunBuilder.build())
                    .build()
        }
    }
}
