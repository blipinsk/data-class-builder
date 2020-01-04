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

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.WildcardTypeName
import kotlinx.metadata.*
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

fun extractKotlinClassHeader(element: Element): KotlinClassHeader? {
    val annotation: Metadata? = element.getAnnotation(Metadata::class.java)
    return annotation?.run {
        KotlinClassHeader(kind, metadataVersion, bytecodeVersion, data1, data2, extraString, packageName, extraInt)
    }
}

fun extractKotlinMetadata(element: Element): KotlinClassMetadata? {
    return extractKotlinClassHeader(element)?.let { KotlinClassMetadata.read(it) }
}

fun KotlinClassMetadata.companionObjectName(): String? {
    if (this is KotlinClassMetadata.Class) {
        return this.toKmClass().companionObject
    }
    return null
}

fun KotlinClassMetadata.isDataClass(): Boolean {
    if (this is KotlinClassMetadata.Class) {
        return this.isDataClass() //redirecting to the other extension fun
    }
    return false
}

fun KotlinClassMetadata.Class.isDataClass(): Boolean {
    return Flag.Class.IS_DATA(this.toKmClass().flags)
}

fun KmProperty.isNullable(): Boolean {
    return Flag.Type.IS_NULLABLE(this.returnType.flags)
}

fun KmProperty.toKotlinpoetTypeName(): TypeName {
    val classifier = this.returnType.classifier as KmClassifier.Class
    val kotlinMetadataClassName = classifier.name
    val jvmCompatibleName = kotlinMetadataClassName.replace("/", ".")
    return ClassName.bestGuess(jvmCompatibleName)
}

fun KmClass.primaryConstructor(): KmConstructor? {
    return constructors.firstOrNull { Flag.Constructor.IS_PRIMARY.invoke(it.flags) }
}

fun KmValueParameter.toKotlinpoetTypeName(): TypeName = this.type!!.toKotlinpoetTypeName()

fun KmType.toKotlinpoetTypeName(): TypeName {
    val classifier = this.classifier as KmClassifier.Class
    val kotlinMetadataClassName = classifier.name
    val jvmCompatibleName = kotlinMetadataClassName.replace("/", ".")
    val baseType = ClassName.bestGuess(jvmCompatibleName)

    return if (this.arguments.isEmpty()) {
        //no arguments -> not a parameterized type
        baseType
    } else {
        val parameters = this.arguments.map { typeProjection ->
            if (typeProjection.type != null) {
                val type = typeProjection.type!!.toKotlinpoetTypeName() //recurrence
                when {
                    typeProjection.variance == KmVariance.INVARIANT -> type
                    typeProjection.variance == KmVariance.OUT -> WildcardTypeName.producerOf(type)
                    else -> error("Illegal variance ${typeProjection.variance}")
                }
            } else {
                //no projection type -> using the <*>
                STAR
            }
        }
        baseType.parameterizedBy(parameters)
    }
}

fun KmValueParameter.isNullable(): Boolean {
    return type?.let { it ->
        return@let Flag.Type.IS_NULLABLE.invoke(it.flags)
    } ?: false //if type missing, reporting as non-null
}

fun KmValueParameter.hasDefaultValue(): Boolean {
    return Flag.ValueParameter.DECLARES_DEFAULT_VALUE.invoke(this.flags)
}