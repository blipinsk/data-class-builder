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
package com.bartoszlipinski.dataclassbuilder.internal;

import java.io.File;

import javax.annotation.processing.ProcessingEnvironment;

import static com.bartoszlipinski.dataclassbuilder.internal.LoggerKt.compilerWarning;

public final class KotlinFiler {
    public static final String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";
    private final String kaptKotlinGeneratedDir;

    private KotlinFiler(ProcessingEnvironment processingEnv) {
        kaptKotlinGeneratedDir = processingEnv.getOptions().get(KAPT_KOTLIN_GENERATED_OPTION_NAME);
        if (kaptKotlinGeneratedDir == null) {
            compilerWarning("Can't find the target directory for generated Kotlin files.");
        }
    }

    public static KotlinFiler with(ProcessingEnvironment processingEnv) {
        return new KotlinFiler(processingEnv);
    }

    public File newFile() {
        if (kaptKotlinGeneratedDir == null) {
            throw new IllegalStateException("Can't generate Kotlin files.");
        }
        return new File(kaptKotlinGeneratedDir);
    }
}
