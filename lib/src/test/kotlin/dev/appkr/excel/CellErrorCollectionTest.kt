package dev.appkr.excel

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import jakarta.validation.ConstraintViolation
import jakarta.validation.constraints.NotBlank
import jakarta.validation.metadata.ConstraintDescriptor
import org.hibernate.validator.internal.engine.path.PathImpl

class CellErrorCollectionTest : StringSpec() {
    init {
        "should correctly calculate actualRowIndex and actualColIndex based on ExcelDataRange" {
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
    }
}
