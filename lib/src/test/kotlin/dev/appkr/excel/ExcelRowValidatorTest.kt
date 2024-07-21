package dev.appkr.excel

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.Validator
import jakarta.validation.constraints.NotBlank
import jakarta.validation.metadata.ConstraintDescriptor
import org.hibernate.validator.internal.engine.path.PathImpl

class ExcelRowValidatorTest : DescribeSpec() {
    init {
        val mockConstraintViolation = mockk<ConstraintViolation<ExcelRow>> {
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
        }

        describe("ExcelRowValidatorTest") {
            val sut = ExcelRowValidator(
                validator = mockk<Validator> {
                    every { validate(any<ExcelRow>()) } returns setOf(mockConstraintViolation)
                },
                cellErrorMessageFormattingVisitor = mockk<CellErrorMessageFormattingVisitor> {
                    every { visit(any()) } returns "Must not be blank"
                },
            )

            context("validate") {
                val excelDataRange = ExcelDataRange(0, 0, 0)
                it("Returns a collection of CellError") {
                    sut.validate(
                        rows = listOf(
                            object : ExcelRow {
                                override fun getColIndexBy(fieldName: String): Int = 0
                            },
                        ),
                        excelDataRange = excelDataRange,
                    )
                        .also {
                            it.errors.size shouldBe 1
                            it.errors.first() shouldBe CellError(
                                rowIndex = 0,
                                colIndex = 0,
                                cellConstraint = "NotBlank",
                                messageArguments = emptyMap(),
                            )
                            it.errors.first().errorMessage shouldBe "Must not be blank"
                        }
                }
            }
        }
    }
}
