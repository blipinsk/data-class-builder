package dataclass.lives.here

import com.bartoszlipinski.dataclassbuilder.DataClassBuilder

@DataClassBuilder
data class EightDefaultValues(
        //nullable, with default null
        val a: Int? = DEFAULT_NULL,
        //nullable, with default non-null
        val b: String? = DEFAULT_STRING,
        //nullable, without default
        val c: Map<Float, String>?,
        //non-nullable, with default
        val d: List<Double> = DEFAULT_LIST_DOUBLE,
        //non-nullable, without default
        val e: Set<String>,
        val f: String = DEFAULT_STRING,
        val g: String = DEFAULT_STRING,
        val h: String = DEFAULT_STRING,
        val i: String = DEFAULT_STRING,
        val j: String = DEFAULT_STRING
) {
    companion object
}