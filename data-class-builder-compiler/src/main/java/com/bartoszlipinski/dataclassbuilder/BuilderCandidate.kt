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

import com.bartoszlipinski.dataclassbuilder.internal.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.TypeName
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.math.pow

class BuilderCandidate private constructor(val originatingElement: TypeElement) {

    companion object {
        const val EXTENSION_BRIDGE_SUFFIX = "_Ktx"
        const val BUILDER_SUFFIX = "_Builder"
        const val BUILDER_DSL_SUFFIX = "_BuilderDsl"
        const val BUILDER_CORE_SUFFIX = "_BuilderCore"

        fun create(element: Element): BuilderCandidate? {
            return if (element is TypeElement) {
                BuilderCandidate(element)
            } else {
                compilerError("${element.simpleName} is not a class.")
                null
            }
        }
    }

    private val metadata: KotlinClassMetadata = extractKotlinMetadata(originatingElement)!!

    val simpleName by lazy { originatingElement.simpleName.toString() }
    val fullyQualifiedName by lazy { originatingElement.qualifiedName.toString() }
    val packageName by lazy { originatingElement.getPackage() }
    val sourceClassName by lazy { ClassName.bestGuess(fullyQualifiedName) }

    private val companionObjectSimpleName by lazy { metadata.companionObjectName() }
    val hasCompanionObject by lazy { companionObjectSimpleName != null }
    val companionClassName: ClassName by lazy {
        require(hasCompanionObject) { "companion object does not exist" }
        ClassName(packageName, "$simpleName.${companionObjectSimpleName!!}")
    }

    val extBridgeSimpleName by lazy { (simpleName + EXTENSION_BRIDGE_SUFFIX).surroundWithBackticksIfNeeded() }
    val builderSimpleName by lazy { (simpleName + BUILDER_SUFFIX).surroundWithBackticksIfNeeded() }
    val builderDslSimpleName by lazy { (simpleName + BUILDER_DSL_SUFFIX).surroundWithBackticksIfNeeded() }
    val builderCoreSimpleName by lazy { (simpleName + BUILDER_CORE_SUFFIX).surroundWithBackticksIfNeeded() }

    val builderInterfaceName by lazy { ClassName(packageName, builderSimpleName) }
    val builderDslInterfaceName by lazy { ClassName(packageName, builderDslSimpleName) }
    val builderCoreClassName by lazy { ClassName(packageName, builderCoreSimpleName) }

    var propertiesWithChangelog: Int = 0
    val properties: List<Property> by lazy {
        val kmClass = (metadata as KotlinClassMetadata.Class).toKmClass()
        val constructorParameters: List<KmValueParameter> =
                kmClass.primaryConstructor()?.valueParameters ?: emptyList()

        val properties = constructorParameters.map { kmValueParameter ->
            Property(
                    kmValueParameter.toKotlinpoetTypeName(),
                    kmValueParameter.isNullable(),
                    kmValueParameter.name,
                    kmValueParameter.hasDefaultValue()
            )
        }

        propertiesWithChangelog = properties.count(requiresChangelogTracking())

        //setting up properties with default values
        properties
                .filter(requiresChangelogTracking())
                .forEachIndexed { index, property ->
                    //generating changelog values as 2^x (0b001, 0b010, 0b100, etc.)
                    property.initChangelogValue((2.0).pow(index).toLong(), propertiesWithChangelog)
                }

        properties
    }

    // changelog is used for tracking two different things:
    // 1. A need of using default values
    // 2. Using nullable values that do not have defaults
    fun changelogTrackingDefaults(): Long {
        var base = 0L
        properties.forEach {
            if (it.hasChangelog() && it.hasDefaultValue) {
                base = base or it.changelogValue
            }
        }
        return base
    }

    fun changelogTracksAnyNonDefaults(): Boolean {
        properties.forEach {
            if (it.hasChangelog() && !it.hasDefaultValue) {
                return true //found first not-default value related changelog
            }
        }
        return false
    }

    val propertiesWithoutDefault: List<Property> by lazy { properties.filter { property -> !property.hasDefaultValue } }
    val propertiesWithDefaultValue: List<Property> by lazy { properties.filter { property -> property.hasDefaultValue } }

    // We track (with changelog) properties that:
    // 1. Have a default value (to choose a proper constructor)
    // 2. Do no have a default value, but are nullable
    private fun requiresChangelogTracking(): (Property) -> Boolean {
        return { property ->
            property.hasDefaultValue || property.isNullable
        }
    }

    private fun String.surroundWithBackticksIfNeeded(): String {
        return if (this.contains('\$')) {
            "`$this`" //surrounding with backticks ``
        } else {
            this
        }
    }

    class Property(
            private val typeName: TypeName,
            val isNullable: Boolean,
            val name: String,
            val hasDefaultValue: Boolean
    ) {
        private var innerChangelogValue: Long? = null
        var changelogValue: Long
            get() {
                return requireNotNull(innerChangelogValue) { "changelogValue accessed before initializing" }
            }
            set(value) {
                innerChangelogValue = value
            }
        lateinit var changelogValueBinary: String

        internal fun initChangelogValue(value: Long, propertiesWithDefaultCount: Int) {
            changelogValue = value
            changelogValueBinary = changelogValue.asBinaryString(propertiesWithDefaultCount)
        }

        fun hasChangelog() = innerChangelogValue != null

        fun asNullableType(): TypeName = typeName.copy(nullable = true)

        fun type(): TypeName = typeName.copy(nullable = isNullable)
    }
}