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

import com.bartoszlipinski.dataclassbuilder.Builder
import com.bartoszlipinski.dataclassbuilder.BuilderCandidate
import com.bartoszlipinski.dataclassbuilder.BuilderDsl
import com.bartoszlipinski.dataclassbuilder.internal.KotlinFiler
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import kotlin.reflect.KClass

class KotlinFileGenerator private constructor(
        packageName: String,
        fileName: String
) {
    companion object {
        internal const val FILE_GENERATION_COMMENT = "Generated code from data-class-builder compiler. Do not modify!"

        internal const val FUN_BUILD = "build"
        internal const val EXT_FUN_BUILD_DATA_CLASS = "buildDataClass"
        internal const val EXT_FUN_DATA_CLASS_BUILDER = "dataClassBuilder"

        internal const val PARAM_KCLASS = "kClass"
        internal const val PARAM_FUNC = "func"

        internal const val PROPERTY_CHANGELOG = "changelog"
        internal const val PRIVATE_PROPERTY_PREFIX = "private"

        internal const val VARIABLE_INSTANCE = "instance"

        internal const val STATEMENT_RETURN_NEW_INSTANCE = "return %T()"
        internal const val STATEMENT_RETURN_THIS = "return this"
        internal const val STATEMENT_RETURN_CREATE = "return %T.create()"

        internal val CLASS_NAME_ANY: ClassName = Any::class.asTypeName()
        internal val CLASS_NAME_UNIT: ClassName = Unit::class.asTypeName()
        internal val CLASS_NAME_KCLASS: ClassName = KClass::class.asTypeName()
        internal val CLASS_NAME_INT: ClassName = Int::class.asTypeName()
        internal val CLASS_NAME_LONG: ClassName = Long::class.asTypeName()
        internal val CLASS_NAME_GENERIC_BUILDER: ClassName = Builder::class.asTypeName()
        internal val CLASS_NAME_GENERIC_BUILDER_DSL: ClassName = BuilderDsl::class.asTypeName()

        internal val MEMBER_NAME_CHECK_NOT_NULL =  MemberName("com.bartoszlipinski.dataclassbuilder", "checkNotNull")
        internal val MEMBER_NAME_CONTAINS_FLAG =  MemberName("com.bartoszlipinski.dataclassbuilder", "containsFlag")
        internal val MEMBER_NAME_CHECK_CONTAINS_FLAG =  MemberName("com.bartoszlipinski.dataclassbuilder", "checkContainsFlag")

        internal val ANNOTATION_SUPPRESS_UNUSED = AnnotationSpec
                .builder(Suppress::class)
                .addMember("%S", "UNUSED_PARAMETER")
                .build()

        fun beginGenerating(packageName: String, fileName: String): KotlinFileGenerator {
            return KotlinFileGenerator(packageName, fileName)
        }
    }

    private val fileBuilder: FileSpec.Builder = FileSpec
            .builder(packageName, fileName)
            .addComment(FILE_GENERATION_COMMENT)

    fun writeFile(kotlinFiler: KotlinFiler) {
        fileBuilder.build().writeTo(kotlinFiler.newFile())
    }

    fun generateBuilderInterface(candidate: BuilderCandidate) {
        val factoryCompanion = TypeSpec.companionObjectBuilder()
                .addFunction(FunSpec.builder("create")
                        .addAnnotation(JvmStatic::class)
                        .addStatement(STATEMENT_RETURN_NEW_INSTANCE, candidate.builderCoreClassName)
                        .returns(candidate.builderInterfaceName)
                        .build())
                .build()

        val interfaceBuilder = TypeSpec.interfaceBuilder(candidate.builderInterfaceName)
                .addSuperinterface(CLASS_NAME_GENERIC_BUILDER.parameterizedBy(candidate.sourceClassName))
                .addType(factoryCompanion)
                .addFunctions(BuilderGenerator.generateDataFunctionSignatures(candidate))
                .addOriginatingElement(candidate.originatingElement)

        fileBuilder.addType(interfaceBuilder.build())
    }

    fun generateBuilderDslInterface(candidate: BuilderCandidate) {
        val factoryCompanion = TypeSpec.companionObjectBuilder()
                .addFunction(FunSpec.builder("create")
                        .addAnnotation(JvmStatic::class)
                        .addStatement(STATEMENT_RETURN_NEW_INSTANCE, candidate.builderCoreClassName)
                        .returns(candidate.builderDslInterfaceName)
                        .build())
                .build()

        val interfaceBuilder = TypeSpec.interfaceBuilder(candidate.builderDslInterfaceName)
                .addSuperinterface(CLASS_NAME_GENERIC_BUILDER_DSL.parameterizedBy(candidate.sourceClassName))
                .addType(factoryCompanion)
                .addProperties(BuilderGenerator.generateDataPropertySignatures(candidate))
                .addOriginatingElement(candidate.originatingElement)

        fileBuilder.addType(interfaceBuilder.build())
    }

    fun generateBuilderCoreClass(candidate: BuilderCandidate) {
        val classBuilder = TypeSpec.classBuilder(candidate.builderCoreSimpleName)
                .primaryConstructor(BuilderGenerator.generateConstructor())
                .addModifiers(KModifier.INTERNAL)
                .addSuperinterface(candidate.builderInterfaceName)
                .addSuperinterface(candidate.builderDslInterfaceName)
                .addOriginatingElement(candidate.originatingElement)

        BuilderGenerator.generateChangelogProperty(candidate)?.let { changelogProperty ->
            classBuilder.addProperty(changelogProperty)
        }

        classBuilder.addProperties(BuilderGenerator.generateDataProperties(candidate))
        classBuilder.addFunctions(BuilderGenerator.generateDataFunctions(candidate))
        classBuilder.addFunction(BuilderGenerator.generateBuildFun(candidate))

        fileBuilder.addType(classBuilder.build())
    }

    fun generateBuilderExtBridge(candidate: BuilderCandidate) {
        val extBridge = FunSpec.builder(EXT_FUN_DATA_CLASS_BUILDER)
                .returns(candidate.builderInterfaceName)
                .receiver(CLASS_NAME_ANY)
                .addParameter(ParameterSpec
                        .builder(
                                PARAM_KCLASS,
                                CLASS_NAME_KCLASS.parameterizedBy(candidate.sourceClassName)
                        )
                        .addAnnotation(ANNOTATION_SUPPRESS_UNUSED)
                        .build()
                )
                .addStatement(STATEMENT_RETURN_CREATE, candidate.builderInterfaceName)
                .addOriginatingElement(candidate.originatingElement)
                .build()

        fileBuilder.addFunction(extBridge)
    }

    fun generateBuilderDslExtBridge(candidate: BuilderCandidate) {
        val extBridge = FunSpec.builder(EXT_FUN_BUILD_DATA_CLASS)
                .returns(candidate.sourceClassName)
                .receiver(CLASS_NAME_ANY)
                .addParameter(ParameterSpec
                        .builder(
                                PARAM_KCLASS,
                                CLASS_NAME_KCLASS.parameterizedBy(candidate.sourceClassName)
                        )
                        .addAnnotation(ANNOTATION_SUPPRESS_UNUSED)
                        .build()
                )
                .addParameter(ParameterSpec
                        .builder(
                                PARAM_FUNC,
                                LambdaTypeName.get(
                                        candidate.builderDslInterfaceName,
                                        listOf(),
                                        CLASS_NAME_UNIT
                                )
                        )
                        .build()
                )
                .addStatement("val instance = %T()", candidate.builderCoreClassName)
                .addStatement("return instance.apply { func() }.build()")
                .addOriginatingElement(candidate.originatingElement)
                .build()

        fileBuilder.addFunction(extBridge)
    }

    fun generateBuilderCompanionExtBridge(candidate: BuilderCandidate) {
        val extBridge = FunSpec.builder(EXT_FUN_DATA_CLASS_BUILDER)
                .returns(candidate.builderInterfaceName)
                .receiver(candidate.companionClassName)
                .addStatement(STATEMENT_RETURN_CREATE, candidate.builderInterfaceName)
                .addOriginatingElement(candidate.originatingElement)
                .build()

        fileBuilder.addFunction(extBridge)
    }

    fun generateBuilderDslCompanionExtBridge(candidate: BuilderCandidate) {
        val extBridge = FunSpec.builder(EXT_FUN_BUILD_DATA_CLASS)
                .returns(candidate.sourceClassName)
                .receiver(candidate.companionClassName)
                .addParameter(ParameterSpec
                        .builder(
                                PARAM_FUNC,
                                LambdaTypeName.get(
                                        candidate.builderDslInterfaceName,
                                        listOf(),
                                        CLASS_NAME_UNIT
                                )
                        )
                        .build()
                )
                .addStatement("val instance = %T()", candidate.builderCoreClassName)
                .addStatement("return instance.apply { func() }.build()")
                .addOriginatingElement(candidate.originatingElement)
                .build()

        fileBuilder.addFunction(extBridge)
    }
}

