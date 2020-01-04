@file:Suppress("ClassName")

package integration.test

class Optional<T>() {
    private var isSet = false
    private var t: T? = null

    constructor(t: T?) : this() {
        this.t = t
        this.isSet = true
    }

    fun set(t: T) {
        isSet = true
        this.t = t
    }

    fun get(): T? {
        require(isSet)
        return t
    }

    fun isPresent() = isSet

    override fun toString(): String {
        return "Optional(isSet=$isSet, t=$t)"
    }
}

data class TestPair<A> internal constructor(
        val input: Optional<A>,
        val expected: A
) {

    /**
     * Returns string representation of the [Pair] including its [input] and [expected] values.
     */
    override fun toString(): String = "($input, $expected)"
}

val failureTestPair = TestPair<Any>(Optional(), Unit)

/**
 * Signals failing to create a data class
 */
object FAILURE
object PROPERTY_NOT_SET
object PROPERTY_SET_TO_NULL
object EXPECTED_NULL

infix fun <A> PROPERTY_SET_TO_NULL.to(that: A): TestPair<A> = TestPair<A>(Optional(null), that)
infix fun <A> PROPERTY_NOT_SET.to(that: A): TestPair<A> = TestPair(Optional(), that)
infix fun <A> PROPERTY_NOT_SET.to(that: FAILURE): TestPair<A> = failureTestPair as TestPair<A>
infix fun <A> PROPERTY_NOT_SET.to(that: EXPECTED_NULL): TestPair<A?> = TestPair(Optional(), null)
infix fun <A> A.to(that: A): TestPair<A> = TestPair(Optional(this), that)