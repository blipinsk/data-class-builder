data-class-builder
===============  
[ ![bintray](https://img.shields.io/bintray/v/blipinsk/maven/data-class-builder?color=success&label=bintray) ](https://bintray.com/blipinsk/maven/data-class-builder/_latestVersion)

🏗 Automatically generating builders :construction_worker: for Kotlin `data classes`.

*(This annotation processor **IS INCREMENTAL**)* 
  
Usage  
=====  
  
*For a working usage of this library see the `sample/` module.*  
  
  
 1. (*optional - for **Android** projects*) add to your app's `build.gradle`:  
    ```groovy  
    android {  
        kotlinOptions.jvmTarget = '1.8'  
    }  
    ```  
  
 2. Add to `@DataClassBuilder` annotation your `data class` (without `companion object`)
    ```kotlin
    @DataClassBuilder  
    data class User(  
        val name: String = "Anonymous",  
        val age: Int,  
        val photoUrl: String? = null  
    ) {  
        companion object //<-- OPTIONAL companion object
    }  
    ```  
       
 3. Use the generated builder to create your `data class`:
    ```kotlin
    //1. Java-style builder
    val jane = dataClassBuilder(User::class)
        .name("Jane")
        .age(12)
        .build()
    ```
    or
    ```kotlin
    //2. Kotlin DSL builder
    val jane = buildDataClass(User::class) {
        name = "Jane"
        age = 12
    }
    ```
    or
    ```kotlin
    //3. companion Java-style builder (generated if companion is present)
    val jane = User.dataClassBuilder()
        .name("Jane")
        .age(12)
        .build()
    ```
    or
    ```kotlin
    //4. companion Kotlin DSL builder (generated if companion is present)
    val jane = User.buildDataClass {
        name = "Jane"
        age = 12
    }
    ```
  
Including In Your Project  
-------------------------  
Add in your `build.gradle`:  
```xml  
repositories {  
    maven { url 'https://dl.bintray.com/blipinsk/maven/' }  
}  
  
dependencies {  
    kapt "com.bartoszlipinski:data-class-builder-compiler:0.1.0"  
    implementation "com.bartoszlipinski:data-class-builder:0.1.0"  
}  
```  
  
Developed by  
============  
 * Bartosz Lipiński  
  
License  
=======  
  
    Copyright 2020 Bartosz Lipiński  
      
    Licensed under the Apache License, Version 2.0 (the "License");  
    you may not use this file except in compliance with the License.  
    You may obtain a copy of the License at  
  
       http://www.apache.org/licenses/LICENSE-2.0  
  
    Unless required by applicable law or agreed to in writing, software  
    distributed under the License is distributed on an "AS IS" BASIS,  
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  
    See the License for the specific language governing permissions and  
    limitations under the License.
