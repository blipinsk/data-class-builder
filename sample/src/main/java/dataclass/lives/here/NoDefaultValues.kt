package dataclass.lives.here

import com.bartoszlipinski.dataclassbuilder.DataClassBuilder

@DataClassBuilder
data class NoDefaultValues(
        //nullable
        val a: Int?,
        //non-nullable
        val b: List<Double>
) {
    companion object
}