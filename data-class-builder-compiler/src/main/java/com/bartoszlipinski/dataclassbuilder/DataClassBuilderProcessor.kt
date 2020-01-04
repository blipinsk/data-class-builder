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

import com.bartoszlipinski.dataclassbuilder.generator.KotlinFileGenerator
import com.bartoszlipinski.dataclassbuilder.internal.*
import com.bartoszlipinski.dataclassbuilder.internal.KotlinFiler.KAPT_KOTLIN_GENERATED_OPTION_NAME
import com.google.auto.common.SuperficialValidation
import com.google.auto.service.AutoService
import net.ltgt.gradle.incap.IncrementalAnnotationProcessor
import net.ltgt.gradle.incap.IncrementalAnnotationProcessorType
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@AutoService(Processor::class)
@IncrementalAnnotationProcessor(IncrementalAnnotationProcessorType.ISOLATING)
@Suppress("unused")
open class DataClassBuilderProcessor : AbstractProcessor() {

    companion object {
        internal val ANNOTATION = DataClassBuilder::class.java
        internal val PROCESSOR_PREFIX = "[${ANNOTATION.simpleName} processor]"

        private const val FILE_GENERATION_COMMENT = "Generated code from data-class-builder compiler. Do not modify!"
        private const val PACKAGE_DATA_CLASS_BUILDER = "com.bartoszlipinski.dataclassbuilder"
    }

    override fun getSupportedAnnotationTypes() = setOf(ANNOTATION.canonicalName)

    override fun getSupportedOptions() = setOf(KAPT_KOTLIN_GENERATED_OPTION_NAME)

    override fun getSupportedSourceVersion(): SourceVersion = SourceVersion.latestSupported()

    override fun process(annotations: Set<TypeElement>, roundEnv: RoundEnvironment): Boolean {
        Logger.retainInstance(processingEnv, PROCESSOR_PREFIX)
        PackageUtils.retainInstance(processingEnv)

        val kotlinFiler = KotlinFiler.with(processingEnv)

        for (candidate in findCandidates(roundEnv)) {

            // classes are generated in class package
            val classesFile = KotlinFileGenerator
                    .beginGenerating(candidate.packageName, candidate.builderSimpleName)

            // extension bridge generated in data-class-builder package for proper resolution
            val extensionBridgeFile = KotlinFileGenerator
                    .beginGenerating(PACKAGE_DATA_CLASS_BUILDER, candidate.extBridgeSimpleName)

            //generating regular Builder
            classesFile.generateBuilderInterface(candidate)

            //generating Builder DSL
            classesFile.generateBuilderDslInterface(candidate)

            //generating Builders core
            classesFile.generateBuilderCoreClass(candidate)

            //generating Extension Bridge for KClasses
            extensionBridgeFile.generateBuilderExtBridge(candidate)
            extensionBridgeFile.generateBuilderDslExtBridge(candidate)

            //generating Extension Bridge for companion object
            if (candidate.hasCompanionObject) {
                extensionBridgeFile.generateBuilderCompanionExtBridge(candidate)
                extensionBridgeFile.generateBuilderDslCompanionExtBridge(candidate)
            } else {
                warningNoCompanion(candidate)
            }

            classesFile.writeFile(kotlinFiler)
            extensionBridgeFile.writeFile(kotlinFiler)
        }

        Logger.clearInstance()
        PackageUtils.clearInstance()
        return true
    }

    private fun findCandidates(roundEnv: RoundEnvironment): Collection<BuilderCandidate> {
        val candidates = mutableListOf<BuilderCandidate>()
        for (element in roundEnv.getElementsAnnotatedWith(ANNOTATION)) {
            if (!SuperficialValidation.validateElement(element)) continue
            if (!KotlinValidation.validateDataClass(element)) continue

            candidates.add(BuilderCandidate.create(element) ?: continue)
        }
        return candidates
    }

    private fun warningNoCompanion(candidate: BuilderCandidate) {
        compilerWarning(
                "No companion object present in `${candidate.fullyQualifiedName}`.\n" +
                        "Not generating companion object extension function.\n" +
                        "Add empty companion object to fix this warning:\n\n" +
                        "@${ANNOTATION.simpleName}\n" +
                        "data class ${candidate.simpleName}(\n" +
                        "        // your properties... \n" +
                        ") {\n" +
                        "    companion object\n" +
                        "}\n"
        )
    }
}
