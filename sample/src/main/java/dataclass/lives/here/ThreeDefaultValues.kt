package dataclass.lives.here

import com.bartoszlipinski.dataclassbuilder.DataClassBuilder

val DEFAULT_NULL = null
const val DEFAULT_STRING = "default value"
val DEFAULT_LIST_DOUBLE = listOf(4.8, 15.16, 23.42)

@DataClassBuilder
data class ThreeDefaultValues(
        //nullable, with default null
        val a: Int? = DEFAULT_NULL,
        //nullable, with default non-null
        val b: String? = DEFAULT_STRING,
        //nullable, without default
        val c: Map<Float, String>?,
        //non-nullable, with default
        val d: List<Double> = DEFAULT_LIST_DOUBLE,
        //non-nullable, without default
        val e: Set<String>
) {
    companion object
}

@DataClassBuilder
data class ThreeDefaultValues_WithoutNullable(
        //nullable, with default null
        val a: Int? = DEFAULT_NULL,
        //nullable, with default non-null
        val b: String? = DEFAULT_STRING,
        //non-nullable, with default
        val c: List<Double> = DEFAULT_LIST_DOUBLE,
        //non-nullable, without default
        val d: Set<String>
) {
    companion object
}