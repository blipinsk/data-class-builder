![Image](/img/2200x660.png)
  
[ ![bintray](https://img.shields.io/bintray/v/blipinsk/maven/data-class-builder?color=success&label=bintray) ](https://bintray.com/blipinsk/maven/data-class-builder/_latestVersion) 
[ ![maven-central](https://img.shields.io/maven-central/v/com.bartoszlipinski/data-class-builder?label=maven-central) ](https://search.maven.org/search?q=g:com.bartoszlipinski%20AND%20a:data-class-builder)

</p>

🏗 Automatically generating builders :construction_worker: for Kotlin `data classes`.

<sub><sup><b><i>(incremental)</b></i></sup></sub>
  
Usage  
=====  
  
*For a working usage of this library see the `sample/` module.*  
  
  
 1. (*optional - for **Android** projects*) add to your app's `build.gradle`:  
    ```groovy  
    android {  
        kotlinOptions.jvmTarget = '1.8'  
    }  
    ```  
  
 2. Add to `@DataClassBuilder` annotation your `data class`
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
        .age(30)
        .build()
    ```
    or
    ```kotlin
    //2. Kotlin DSL builder
    val jane = buildDataClass(User::class) {
        name = "Jane"
        age = 30
    }
    ```
    or
    ```kotlin
    //3. Java-style builder through companion (generated if companion is present)
    val jane = User.dataClassBuilder()
        .name("Jane")
        .age(30)
        .build()
    ```
    or
    ```kotlin
    //4. Kotlin DSL builder through companion (generated if companion is present)
    val jane = User.buildDataClass {
        name = "Jane"
        age = 30
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


Important
---------
The presence of `data class` parameters is verified **in runtime**.

E.g.

 1. For `data class`:
    ```kotlin
    @DataClassBuilder  
    data class User(  
        val name: String = "Anonymous",  
        val age: Int // <-- no default value specified
    )
    ```
    
 2. When creating:
    ```kotlin
    buildDataClass(User::class) {
        name = "John"
        // not setting the necessary `age`
    }
    ```

3. :point_up: this will crash :boom: **in runtime** :point_up:
  
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
