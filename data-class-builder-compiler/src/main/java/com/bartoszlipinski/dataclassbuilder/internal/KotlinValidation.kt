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

import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element

object KotlinValidation {

    // api

    fun validateDataClass(element: Element): Boolean {
        val metadata = extractKotlinMetadata(element)

        if (metadata == null) {
            errorMustBeKotlinClass(element)
            return false
        }

        if (metadata !is KotlinClassMetadata.Class) {
            //not a class, therefore not a data class as well -> error
            errorMustBeDataClass(element)
            return false
        }


        if (!metadata.isDataClass()) {
            errorMustBeDataClass(element)
            return false
        }

        return true
    }

    // internal

    private fun errorMustBeKotlinClass(element: Element) {
        compilerError("${element.simpleName}: must be a Kotlin data class.")
    }

    private fun errorMustBeDataClass(element: Element) {
        compilerError("${element.simpleName}: must be a data class.")
    }
}