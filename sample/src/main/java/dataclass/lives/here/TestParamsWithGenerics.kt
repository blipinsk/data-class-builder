package dataclass.lives.here

import com.bartoszlipinski.dataclassbuilder.DataClassBuilder

@DataClassBuilder
data class TestParamsWithGenerics(
        val a: Set<*>,
        val b: Set<out Runnable>,
        val c: () -> Unit
) {
    companion object
}