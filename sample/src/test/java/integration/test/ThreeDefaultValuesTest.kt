package integration.test

import com.bartoszlipinski.dataclassbuilder.DataClassBuilderException
import com.bartoszlipinski.dataclassbuilder.buildDataClass
import com.bartoszlipinski.dataclassbuilder.dataClassBuilder
import com.google.common.truth.Truth.assertWithMessage
import dataclass.lives.here.DEFAULT_LIST_DOUBLE
import dataclass.lives.here.DEFAULT_STRING
import dataclass.lives.here.ThreeDefaultValues
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class ThreeDefaultValuesTestCase(
        val description: String,
        val a: TestPair<Int?>,
        val b: TestPair<String?>,
        val c: TestPair<Map<Float, String>?>,
        val d: TestPair<List<Double>>,
        val e: TestPair<Set<String>>
) {
    fun setsA() = a.input.isPresent()
    fun setsB() = b.input.isPresent()
    fun setsC() = c.input.isPresent()
    fun setsD() = d.input.isPresent()
    fun setsE() = e.input.isPresent()

    fun expectsFailure(): Boolean {
        // 'c' and 'e' do not have defaults so they can be failures
        return c == failureTestPair || e == failureTestPair
    }

    override fun toString(): String = description
}

@RunWith(Parameterized::class)
class ThreeDefaultValuesTest {

    companion object {
        private val EXAMPLE_MAP = mapOf(Pair(2.1f, "abcde"), Pair(11.1f, "cdefg"))
        private val EXAMPLE_LIST_OF_DOUBLE = listOf(2.1, 11.1)
        private val EXAMPLE_STRING_SET = setOf("Abcd", "cdef")

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): Collection<Array<ThreeDefaultValuesTestCase>> = listOf(
                arrayOf(ThreeDefaultValuesTestCase(
                        "No default params used, nullable map set to null",
                        3 to 3,
                        "Test" to "Test",
                        PROPERTY_SET_TO_NULL to null,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase(
                        "No default params used, nullable map set to not-null",
                        3 to 3,
                        "Test" to "Test",
                        EXAMPLE_MAP to EXAMPLE_MAP,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase(
                        "All default params used",
                        PROPERTY_NOT_SET to EXPECTED_NULL,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_NOT_SET to DEFAULT_LIST_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase(
                        "All default params used (nulls for fields with default)",
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_NOT_SET to DEFAULT_LIST_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase(
                        "Required property 'c' missing",
                        3 to 3,
                        "test" to "test",
                        PROPERTY_NOT_SET to FAILURE, //required property not set -> failure
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase(
                        "Required property 'e' missing",
                        3 to 3,
                        "test" to "test",
                        EXAMPLE_MAP to EXAMPLE_MAP,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        PROPERTY_NOT_SET to FAILURE //required property not set -> failure
                ))
        )
    }

    @Parameterized.Parameter
    lateinit var testCase: ThreeDefaultValuesTestCase

    @Test
    fun dataClassBuilder_worksCorrectly_whenAccessedWithGenerics() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            dataClassBuilder(ThreeDefaultValues::class)
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
            ThreeDefaultValues.dataClassBuilder()
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
            buildDataClass(ThreeDefaultValues::class) {
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
            ThreeDefaultValues.buildDataClass {
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
            }
        }

        //then
        assert(dataClass).matches(testCase)
    }

    private fun assert(dataClass: ThreeDefaultValues?) = AssertDataClass(dataClass)

    private data class AssertDataClass(val dataClass: ThreeDefaultValues?) {
        fun matches(testCase: ThreeDefaultValuesTestCase) {
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
            }
        }
    }
}