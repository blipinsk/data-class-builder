package one.pckg

import one.pckg.nested.TryingSomethingElse

//@AutoBuilder
data class DataClassWithCompanionObject(
        val a1: String = "dupa",
        val b1: Int? = 7,
        val d1: TryingSomethingElse
) {
    constructor(
            a1: String = "dupa",
            b1: Int? = 7,
            c1: Int? = 7,
            d1: TryingSomethingElse
    ) : this(a1, b1!! + c1!!, d1)

    companion object {
        //        @AutoBuilder
//        data class TestAAA(
//                val a1: String,
//                val b1: Int? = 3,
//                val c1: String,
//                val d1: TryingSomethingElse?
//        )
    }
}