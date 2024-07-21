package dev.appkr.excel

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.datatest.withData
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.constraints.AssertFalse
import jakarta.validation.constraints.AssertTrue
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.FutureOrPresent
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Negative
import jakarta.validation.constraints.NegativeOrZero
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Null
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.PastOrPresent
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size
import jakarta.validation.metadata.ConstraintDescriptor
import org.hibernate.validator.internal.engine.path.PathImpl

class CellErrorCollectionTest : DescribeSpec() {
    init {
        describe("CellErrorCollectionTest") {
            it("should correctly calculate actualRowIndex and actualColIndex based on ExcelDataRange") {
                val cellErrors = CellErrorCollection(
                    errors = listOf(
                        CellError.of(
                            rowIndex = 1,
                            colIndex = 0,
                            constraintViolation = mockk<ConstraintViolation<ExcelRow>> {
                                every { message } returns "Must not be blank"
                                every { rootBeanClass } returns ExcelRow::class.java
                                every { propertyPath } returns PathImpl.createPathFromString("exampleField")
                                every { leafBean } returns Any()
                                every { invalidValue } returns ""
                                every { constraintDescriptor } returns mockk<ConstraintDescriptor<*>> {
                                    every { annotation } returns mockk<NotBlank> {
                                        every { annotationClass.java } returns NotBlank::class.java
                                    }
                                    every { attributes } returns mapOf<String, Any>()
                                }
                            },
                        ),
                    ),
                    excelDataRange = ExcelDataRange(startRow = 5, startCol = 1, endCol = 10),
                )

                val actualCellError = cellErrors.errors.first()

                actualCellError.actualRowIndex shouldBe 6
                actualCellError.actualColIndex shouldBe 0
            }

            context("When instantiate CellError object") {
                withData(
                    nameFn = { it.first.simpleName },
                    ts = constraints,
                ) { (annotationClass, message) ->
                    val cellError = CellError.of(
                        rowIndex = 0,
                        colIndex = 0,
                        constraintViolation = createMockConstraintViolation(annotationClass, message),
                    )

                    val expectedMap: Map<String, Any> = when (annotationClass) {
                        AssertFalse::class.java -> emptyMap()
                        AssertTrue::class.java -> emptyMap()
                        DecimalMax::class.java -> emptyMap()
                        DecimalMin::class.java -> emptyMap()
                        Digits::class.java -> emptyMap()
                        Email::class.java -> emptyMap()
                        Future::class.java -> emptyMap()
                        FutureOrPresent::class.java -> emptyMap()
                        Max::class.java -> emptyMap()
                        Min::class.java -> emptyMap()
                        Negative::class.java -> emptyMap()
                        NegativeOrZero::class.java -> emptyMap()
                        NotBlank::class.java -> emptyMap()
                        NotEmpty::class.java -> emptyMap()
                        NotNull::class.java -> emptyMap()
                        Null::class.java -> emptyMap()
                        Past::class.java -> emptyMap()
                        PastOrPresent::class.java -> emptyMap()
                        Pattern::class.java -> emptyMap()
                        Positive::class.java -> emptyMap()
                        PositiveOrZero::class.java -> emptyMap()
                        Size::class.java -> emptyMap()
                        else -> throw IllegalArgumentException("Unknown annotation class")
                    }

                    val result = cellError.messageArguments

                    result shouldContainAll expectedMap
                }
            }
        }
    }

    private val constraints = listOf(
        NotBlank::class.java to "Must not be blank",
        NotEmpty::class.java to "Must not be empty",
        NotNull::class.java to "Must not be null",
        AssertFalse::class.java to "Must be false",
        AssertTrue::class.java to "Must be true",
        DecimalMax::class.java to "Must be less than or equal to max",
        DecimalMin::class.java to "Must be greater than or equal to min",
        Digits::class.java to "Must be within digits constraints",
        Email::class.java to "Must be a valid email",
        Future::class.java to "Must be in the future",
        FutureOrPresent::class.java to "Must be in the present or future",
        Max::class.java to "Must be less than or equal to max",
        Min::class.java to "Must be greater than or equal to min",
        Negative::class.java to "Must be negative",
        NegativeOrZero::class.java to "Must be negative or zero",
        Null::class.java to "Must be null",
        Past::class.java to "Must be in the past",
        PastOrPresent::class.java to "Must be in the past or present",
        Pattern::class.java to "Must match pattern",
        Positive::class.java to "Must be positive",
        PositiveOrZero::class.java to "Must be positive or zero",
        Size::class.java to "Size must be between min and max",
    )

    fun createMockConstraintViolation(annotationClass: Class<out Annotation>, message: String): ConstraintViolation<ExcelRow> =
        mockk {
            every { this@mockk.message } returns message
            every { rootBeanClass } returns ExcelRow::class.java
            every { propertyPath } returns PathImpl.createPathFromString("exampleField")
            every { leafBean } returns Any()
            every { invalidValue } returns ""
            every { constraintDescriptor } returns mockk {
                every { annotation } returns annotationClass.annotations.first()
                every { attributes } returns mapOf<String, Any>()
            }
        }
}
