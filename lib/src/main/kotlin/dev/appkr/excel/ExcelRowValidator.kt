package dev.appkr.excel

import jakarta.validation.Validator

class ExcelRowValidator(
    private val validator: Validator,
    private val cellErrorMessageFormattingVisitor: CellErrorMessageFormattingVisitor,
) {
    /**
     * Validates the ExcelRow and collects CellErrors if there are any
     */
    fun <T : ExcelRow> validate(
        rows: Collection<T>,
        excelDataRange: ExcelDataRange,
    ): CellErrorCollection =
        rows
            .flatMapIndexed { rowIndex, row ->
                validator.validate(row)
                    .map {
                        CellError.of(
                            rowIndex = rowIndex,
                            colIndex = row.getColIndexBy(it.propertyPath.toString()),
                            constraintViolation = it,
                        )
                            .apply { updateErrorMessage(cellErrorMessageFormattingVisitor) }
                    }
            }
            .let {
                CellErrorCollection(
                    errors = it,
                    excelDataRange = excelDataRange,
                )
            }
}
