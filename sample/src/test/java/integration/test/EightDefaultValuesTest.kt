package integration.test

import com.bartoszlipinski.dataclassbuilder.DataClassBuilderException
import com.bartoszlipinski.dataclassbuilder.buildDataClass
import com.bartoszlipinski.dataclassbuilder.dataClassBuilder
import com.google.common.truth.Truth.assertWithMessage
import dataclass.lives.here.DEFAULT_LIST_DOUBLE
import dataclass.lives.here.DEFAULT_STRING
import dataclass.lives.here.EightDefaultValues
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class EightDefaultValuesTestCase(
        val description: String,
        val a: TestPair<Int?>,
        val b: TestPair<String?>,
        val c: TestPair<Map<Float, String>?>,
        val d: TestPair<List<Double>>,
        val e: TestPair<Set<String>>,
        val f: TestPair<String>,
        val g: TestPair<String>,
        val h: TestPair<String>,
        val i: TestPair<String>,
        val j: TestPair<String>
) {
    fun setsA() = a.input.isPresent()
    fun setsB() = b.input.isPresent()
    fun setsC() = c.input.isPresent()
    fun setsD() = d.input.isPresent()
    fun setsE() = e.input.isPresent()
    fun setsF() = f.input.isPresent()
    fun setsG() = g.input.isPresent()
    fun setsH() = h.input.isPresent()
    fun setsI() = i.input.isPresent()
    fun setsJ() = j.input.isPresent()

    fun expectsFailure(): Boolean {
        // 'c' and 'e' do not have defaults so they can be failures
        return c == failureTestPair || e == failureTestPair
    }

    override fun toString(): String = description
}

@RunWith(Parameterized::class)
class EightDefaultValuesTest {

    companion object {
        private val EXAMPLE_MAP = mapOf(Pair(2.1f, "abcde"), Pair(11.1f, "cdefg"))
        private val EXAMPLE_LIST_OF_DOUBLE = listOf(2.1, 11.1)
        private val EXAMPLE_STRING_SET = setOf("Abcd", "cdef")

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): Collection<Array<EightDefaultValuesTestCase>> = listOf(
                arrayOf(EightDefaultValuesTestCase(
                        "No default params used, nullable map set to null",
                        3 to 3,
                        "test" to "test",
                        PROPERTY_SET_TO_NULL to null,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET,
                        "test_g" to "test_g",
                        "test_g" to "test_g",
                        "test_h" to "test_h",
                        "test_i" to "test_i",
                        "test_j" to "test_j"
                )),
                arrayOf(EightDefaultValuesTestCase(
                        "No default params used, nullable map set to not-null",
                        3 to 3,
                        "test" to "test",
                        EXAMPLE_MAP to EXAMPLE_MAP,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET,
                        "test_g" to "test_g",
                        "test_g" to "test_g",
                        "test_h" to "test_h",
                        "test_i" to "test_i",
                        "test_j" to "test_j"
                )),
                arrayOf(EightDefaultValuesTestCase(
                        "All default params used",
                        PROPERTY_NOT_SET to EXPECTED_NULL,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        EXAMPLE_MAP to EXAMPLE_MAP,
                        PROPERTY_NOT_SET to DEFAULT_LIST_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_NOT_SET to DEFAULT_STRING
                )),
                arrayOf(EightDefaultValuesTestCase(
                        "Required property 'c' missing",
                        3 to 3,
                        "test" to "test",
                        PROPERTY_NOT_SET to FAILURE, //required property not set -> failure
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET,
                        "test_g" to "test_g",
                        "test_g" to "test_g",
                        "test_h" to "test_h",
                        "test_i" to "test_i",
                        "test_j" to "test_j"
                )),
                arrayOf(EightDefaultValuesTestCase(
                        "Required property 'e' missing",
                        3 to 3,
                        "test" to "test",
                        EXAMPLE_MAP to EXAMPLE_MAP,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        PROPERTY_NOT_SET to FAILURE, //required property not set -> failure
                        "test_g" to "test_g",
                        "test_g" to "test_g",
                        "test_h" to "test_h",
                        "test_i" to "test_i",
                        "test_j" to "test_j"
                ))
        )
    }

    @Parameterized.Parameter
    lateinit var testCase: EightDefaultValuesTestCase

    @Test
    fun dataClassBuilder_worksCorrectly_whenAccessedWithGenerics() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            dataClassBuilder(EightDefaultValues::class)
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get())
                        if (testCase.setsC())
                            c(testCase.c.input.get())
                        if (testCase.setsD())
                            d(testCase.d.input.get()!!)
                        if (testCase.setsE())
                            e(testCase.e.input.get()!!)
                        if (testCase.setsF())
                            f(testCase.f.input.get()!!)
                        if (testCase.setsG())
                            g(testCase.g.input.get()!!)
                        if (testCase.setsH())
                            h(testCase.h.input.get()!!)
                        if (testCase.setsI())
                            i(testCase.i.input.get()!!)
                        if (testCase.setsJ())
                            j(testCase.j.input.get()!!)
                    }
                    .build()
        }

        //then
        assert(dataClass).matches(testCase)
    }

    @Test
    fun dataClassBuilder_worksCorrectly_whenAccessedFromCompanion() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            EightDefaultValues.dataClassBuilder()
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get())
                        if (testCase.setsC())
                            c(testCase.c.input.get())
                        if (testCase.setsD())
                            d(testCase.d.input.get()!!)
                        if (testCase.setsE())
                            e(testCase.e.input.get()!!)
                        if (testCase.setsF())
                            f(testCase.f.input.get()!!)
                        if (testCase.setsG())
                            g(testCase.g.input.get()!!)
                        if (testCase.setsH())
                            h(testCase.h.input.get()!!)
                        if (testCase.setsI())
                            i(testCase.i.input.get()!!)
                        if (testCase.setsJ())
                            j(testCase.j.input.get()!!)
                    }
                    .build()
        }

        //then
        assert(dataClass).matches(testCase)
    }

    @Test
    fun buildDataClass_worksCorrectly_whenAccessedWithGenerics() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            EightDefaultValues.buildDataClass {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()
                if (testCase.setsC())
                    c = testCase.c.input.get()
                if (testCase.setsD())
                    d = testCase.d.input.get()!!
                if (testCase.setsE())
                    e = testCase.e.input.get()!!
                if (testCase.setsF())
                    f = testCase.f.input.get()!!
                if (testCase.setsG())
                    g = testCase.g.input.get()!!
                if (testCase.setsH())
                    h = testCase.h.input.get()!!
                if (testCase.setsI())
                    i = testCase.i.input.get()!!
                if (testCase.setsJ())
                    j = testCase.j.input.get()!!
            }
        }

        //then
        assert(dataClass).matches(testCase)
    }

    @Test
    fun buildDataClass_worksCorrectly_whenAccessedFromCompanion() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            buildDataClass(EightDefaultValues::class) {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()
                if (testCase.setsC())
                    c = testCase.c.input.get()
                if (testCase.setsD())
                    d = testCase.d.input.get()!!
                if (testCase.setsE())
                    e = testCase.e.input.get()!!
                if (testCase.setsF())
                    f = testCase.f.input.get()!!
                if (testCase.setsG())
                    g = testCase.g.input.get()!!
                if (testCase.setsH())
                    h = testCase.h.input.get()!!
                if (testCase.setsI())
                    i = testCase.i.input.get()!!
                if (testCase.setsJ())
                    j = testCase.j.input.get()!!
            }
        }

        //then
        assert(dataClass).matches(testCase)
    }

    private fun assert(dataClass: EightDefaultValues?) = AssertDataClass(dataClass)

    private data class AssertDataClass(val dataClass: EightDefaultValues?) {
        fun matches(testCase: EightDefaultValuesTestCase) {
            if (testCase.expectsFailure()) {
                val throwable = CatchThrowable.caughtThrowable()
                assertWithMessage(testCase.description).that(throwable).isNotNull()
                assertWithMessage(testCase.description).that(throwable).isInstanceOf(DataClassBuilderException::class.java)
                assertWithMessage(testCase.description).that(throwable!!.message).contains("Required parameter")
            } else {
                if (dataClass == null) {
                    val throwable = CatchThrowable.caughtThrowable().let {
                        if (it == null) "N/A" else "throwable=${it::class.java.canonicalName} message=${it.message}"
                    }
                    assertWithMessage("\nTest case: [${testCase.description}] \nUnexpected error: [$throwable]").fail()
                }

                assertWithMessage(testCase.description).that(dataClass!!.a).isEqualTo(testCase.a.expected)
                assertWithMessage(testCase.description).that(dataClass.b).isEqualTo(testCase.b.expected)
                assertWithMessage(testCase.description).that(dataClass.c).isEqualTo(testCase.c.expected)
                assertWithMessage(testCase.description).that(dataClass.d).isEqualTo(testCase.d.expected)
                assertWithMessage(testCase.description).that(dataClass.e).isEqualTo(testCase.e.expected)
                assertWithMessage(testCase.description).that(dataClass.f).isEqualTo(testCase.f.expected)
                assertWithMessage(testCase.description).that(dataClass.g).isEqualTo(testCase.g.expected)
                assertWithMessage(testCase.description).that(dataClass.h).isEqualTo(testCase.h.expected)
                assertWithMessage(testCase.description).that(dataClass.i).isEqualTo(testCase.i.expected)
                assertWithMessage(testCase.description).that(dataClass.j).isEqualTo(testCase.j.expected)
            }
        }
    }
}