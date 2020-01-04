package integration.test

import com.bartoszlipinski.dataclassbuilder.DataClassBuilderException
import com.bartoszlipinski.dataclassbuilder.buildDataClass
import com.bartoszlipinski.dataclassbuilder.dataClassBuilder
import com.google.common.truth.Truth.assertWithMessage
import dataclass.lives.here.DEFAULT_LIST_DOUBLE
import dataclass.lives.here.DEFAULT_STRING
import dataclass.lives.here.ThreeDefaultValues_WithoutNullable
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class ThreeDefaultValuesTestCase_WithoutNullable(
        val description: String,
        val a: TestPair<Int?>,
        val b: TestPair<String?>,
        val c: TestPair<List<Double>>,
        val d: TestPair<Set<String>>
) {
    fun setsA() = a.input.isPresent()
    fun setsB() = b.input.isPresent()
    fun setsC() = c.input.isPresent()
    fun setsD() = d.input.isPresent()

    fun expectsFailure(): Boolean {
        // 'd' does not have defauls so it can be failure
        return d == failureTestPair
    }

    override fun toString(): String = description
}

@RunWith(Parameterized::class)
class ThreeDefaultValuesTest_WithoutNullable {

    companion object {
        private val EXAMPLE_LIST_OF_DOUBLE = listOf(2.1, 11.1)
        private val EXAMPLE_STRING_SET = setOf("Abcd", "cdef")

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): Collection<Array<ThreeDefaultValuesTestCase_WithoutNullable>> = listOf(
                arrayOf(ThreeDefaultValuesTestCase_WithoutNullable(
                        "No default params used",
                        3 to 3,
                        "Test" to "Test",
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase_WithoutNullable(
                        "All default params used",
                        PROPERTY_NOT_SET to EXPECTED_NULL,
                        PROPERTY_NOT_SET to DEFAULT_STRING,
                        PROPERTY_NOT_SET to DEFAULT_LIST_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase_WithoutNullable(
                        "All default params used (nulls for fields with default)",
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_SET_TO_NULL to null,
                        PROPERTY_NOT_SET to DEFAULT_LIST_DOUBLE,
                        EXAMPLE_STRING_SET to EXAMPLE_STRING_SET
                )),
                arrayOf(ThreeDefaultValuesTestCase_WithoutNullable(
                        "Required property 'd' missing",
                        3 to 3,
                        "test" to "test",
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE,
                        PROPERTY_NOT_SET to FAILURE //required property not set -> failure
                ))
        )
    }

    @Parameterized.Parameter
    lateinit var testCase: ThreeDefaultValuesTestCase_WithoutNullable

    @Test
    fun dataClassBuilder_worksCorrectly_whenAccessedWithGenerics() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            dataClassBuilder(ThreeDefaultValues_WithoutNullable::class)
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get())
                        if (testCase.setsC())
                            c(testCase.c.input.get()!!)
                        if (testCase.setsD())
                            d(testCase.d.input.get()!!)
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
            ThreeDefaultValues_WithoutNullable.dataClassBuilder()
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get())
                        if (testCase.setsC())
                            c(testCase.c.input.get()!!)
                        if (testCase.setsD())
                            d(testCase.d.input.get()!!)
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
            buildDataClass(ThreeDefaultValues_WithoutNullable::class) {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()
                if (testCase.setsC())
                    c = testCase.c.input.get()!!
                if (testCase.setsD())
                    d = testCase.d.input.get()!!
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
            ThreeDefaultValues_WithoutNullable.buildDataClass {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()
                if (testCase.setsC())
                    c = testCase.c.input.get()!!
                if (testCase.setsD())
                    d = testCase.d.input.get()!!
            }
        }

        //then
        assert(dataClass).matches(testCase)
    }

    private fun assert(dataClass: ThreeDefaultValues_WithoutNullable?) = AssertDataClass(dataClass)

    private data class AssertDataClass(val dataClass: ThreeDefaultValues_WithoutNullable?) {
        fun matches(testCase: ThreeDefaultValuesTestCase_WithoutNullable) {
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
            }
        }
    }
}