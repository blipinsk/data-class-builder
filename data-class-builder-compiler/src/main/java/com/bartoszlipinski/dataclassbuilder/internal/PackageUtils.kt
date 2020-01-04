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

import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element

object PackageUtils {
    internal var retainedInstance: PackageUtilsImpl? = null

    @Synchronized
    fun retainInstance(pe: ProcessingEnvironment) {
        if (retainedInstance == null) {
            retainedInstance = PackageUtilsImpl(pe)
        }
    }

    @Synchronized
    fun clearInstance() {
        retainedInstance = null
    }
}

internal class PackageUtilsImpl internal constructor(pe: ProcessingEnvironment) {
    internal val utils = pe.elementUtils
}

fun Element.getPackage(): String {
    return PackageUtils.retainedInstance.let { instance ->
        checkNotNull(instance) { "No retained instance" }
        instance.utils.getPackageOf(this).qualifiedName.toString()
    }
}
