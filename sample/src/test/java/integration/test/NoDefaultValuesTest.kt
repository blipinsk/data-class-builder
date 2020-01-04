package integration.test

import com.bartoszlipinski.dataclassbuilder.DataClassBuilderException
import com.bartoszlipinski.dataclassbuilder.buildDataClass
import com.bartoszlipinski.dataclassbuilder.dataClassBuilder
import com.google.common.truth.Truth.assertWithMessage
import dataclass.lives.here.NoDefaultValues
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

data class NoDefaultValuesTestCase(
        val description: String,
        val a: TestPair<Int?>,
        val b: TestPair<List<Double>>
) {
    fun setsA() = a.input.isPresent()
    fun setsB() = b.input.isPresent()

    fun expectsFailure(): Boolean {
        // 'a' and 'b' do not have defaults so they can be failures
        return a == failureTestPair || b == failureTestPair
    }

    override fun toString(): String = description
}

@RunWith(Parameterized::class)
class NoDefaultValuesTest {

    companion object {
        private val EXAMPLE_LIST_OF_DOUBLE = listOf(2.1, 11.1)

        @JvmStatic
        @Parameterized.Parameters(name = "{0}")
        fun testCases(): Collection<Array<NoDefaultValuesTestCase>> = listOf(
                arrayOf(NoDefaultValuesTestCase(
                        "Nullable property set to non-null value",
                        3 to 3,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE
                )),
                arrayOf(NoDefaultValuesTestCase(
                        "Nullable property set to null value",
                        PROPERTY_SET_TO_NULL to null,
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE
                )),
                arrayOf(NoDefaultValuesTestCase(
                        "Required property 'a' missing",
                        PROPERTY_NOT_SET to FAILURE, //required property not set -> failure
                        EXAMPLE_LIST_OF_DOUBLE to EXAMPLE_LIST_OF_DOUBLE
                )),
                arrayOf(NoDefaultValuesTestCase(
                        "Required property 'b' missing",
                        3 to 3,
                        PROPERTY_NOT_SET to FAILURE //required property not set -> failure
                ))
        )
    }

    @Parameterized.Parameter
    lateinit var testCase: NoDefaultValuesTestCase

    @Test
    fun dataClassBuilder_worksCorrectly_whenAccessedWithGenerics() {
        //given -> testCase property

        //when
        val dataClass = CatchThrowable.catchThrowable {
            dataClassBuilder(NoDefaultValues::class)
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get()!!)
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
            NoDefaultValues.dataClassBuilder()
                    .apply {
                        if (testCase.setsA())
                            a(testCase.a.input.get())
                        if (testCase.setsB())
                            b(testCase.b.input.get()!!)
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
            buildDataClass(NoDefaultValues::class) {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()!!
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
            NoDefaultValues.buildDataClass {
                if (testCase.setsA())
                    a = testCase.a.input.get()
                if (testCase.setsB())
                    b = testCase.b.input.get()!!
            }
        }

        //then
        assert(dataClass).matches(testCase)
    }

    private fun assert(dataClass: NoDefaultValues?) = AssertDataClass(dataClass)

    private data class AssertDataClass(val dataClass: NoDefaultValues?) {
        fun matches(testCase: NoDefaultValuesTestCase) {
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
            }
        }
    }
}