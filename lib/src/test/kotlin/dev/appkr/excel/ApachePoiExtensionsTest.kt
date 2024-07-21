package dev.appkr.excel

import io.kotest.core.spec.style.DescribeSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook

class ApachePoiExtensionsTest : DescribeSpec() {
    private lateinit var workbook: Workbook
    private lateinit var sheet: Sheet

    init {
        describe("ApachePoiExtensionsTest") {
            context("clearErrors") {
                beforeEach {
                    sheet.createRow(0).createCell(0)
                        .apply {
                            cellStyle = workbook.createCellStyle()
                                .apply {
                                    fillForegroundColor = IndexedColors.YELLOW.index
                                    fillPattern = FillPatternType.SOLID_FOREGROUND
                                }
                            cellComment = mockk(relaxed = true)
                        }
                }

                it("Clears all the error formattings") {
                    sheet.clearErrors(0, 0, 0, 0)

                    sheet.getRow(0).getCell(0)
                        .also {
                            it.cellStyle.fillForegroundColor shouldBe IndexedColors.AUTOMATIC.index
                            it.cellStyle.fillPattern shouldBe FillPatternType.NO_FILL
                            it.cellComment shouldBe null
                        }
                }
            }

            context("markError") {
                beforeEach {
                    sheet.createRow(0).createCell(0)
                }

                it("Marks error on the cell") {
                    sheet.markError(
                        CellError(
                            rowIndex = 0,
                            colIndex = 0,
                            cellConstraint = "NotBlank",
                            messageArguments = mapOf("message" to "NotBlank"),
                        )
                            .apply {
                                errorMessage = "Must not be blank"
                            },
                    )

                    sheet.getRow(0).getCell(0)
                        .also {
                            it.cellStyle.fillForegroundColor shouldBe IndexedColors.LEMON_CHIFFON.index
                            it.cellStyle.fillPattern shouldBe FillPatternType.SOLID_FOREGROUND
                            it.cellComment.string.toString() shouldBe "Must not be blank"
                            it.cellComment.author shouldBe "YOUR NAME"
                        }
                }
            }

            context("hasAnyBlankRows: when 0th row is blank") {
                beforeEach {
                    sheet.createRow(0).createCell(0)
                    sheet.createRow(1).createCell(0).setCellValue("Data")
                    sheet.createRow(2).createCell(0).setCellValue("Data")
                    sheet.createRow(3).createCell(0).setCellValue("Data")
                }

                it("Says it's blank row") {
                    sheet.hasAnyBlankRows(0, 0, 3) shouldBe true
                }
            }

            context("hasAnyBlankRows: when 0th row is not blank and no data thereafter") {
                beforeEach {
                    sheet.createRow(0).createCell(0).setCellValue("Data")
                    sheet.createRow(1).createCell(0)
                    sheet.createRow(2).createCell(0)
                    sheet.createRow(3).createCell(0)
                }

                it("Says it's no blank rows") {
                    sheet.hasAnyBlankRows(0, 0, 3) shouldBe false
                }
            }

            context("hasAnyBlankRows: when there is any blank row in between") {
                beforeEach {
                    sheet.createRow(0).createCell(0).setCellValue("Data")
                    sheet.createRow(1).createCell(0).setCellValue("Data")
                    sheet.createRow(2).createCell(0)
                    sheet.createRow(3).createCell(0).setCellValue("Data")
                }

                it("Says it's blank row") {
                    sheet.hasAnyBlankRows(0, 0, 3) shouldBe true
                }
            }
        }
    }

    override suspend fun beforeEach(testCase: TestCase) {
        workbook = XSSFWorkbook()
        sheet = workbook.createSheet("TestSheet")
    }
}
