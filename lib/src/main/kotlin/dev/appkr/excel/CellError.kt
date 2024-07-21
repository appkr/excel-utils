package dev.appkr.excel

import jakarta.validation.ConstraintViolation
import jakarta.validation.constraints.Digits
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CellErrorCollection(
    val errors: Collection<CellError>,

    /**
     * The full Excel data range that encompassing this error
     * This field will be used to calculate exact cell address of the Excel
     **/
    val excelDataRange: ExcelDataRange,
) {
    init {
        errors.forEach { cellError ->
            cellError.actualRowIndex = excelDataRange.startRow + cellError.rowIndex
            cellError.actualColIndex = cellError.colIndex
        }
    }
}

data class CellError(
    /** Row index (Zero-based) */
    val rowIndex: Int,

    /** Column index (Zero-based) */
    val colIndex: Int,

    /** The jakarta bean validation constraint that violates the declared rule e.g. Max, NotBlank */
    val cellConstraint: String,

    /** Values to be bound to the error message */
    val messageArguments: Map<String, Any>,
) {
    /**
     * Exact row index(Zero-based)
     */
    var actualRowIndex: Int = 0

    /**
     * Exact column index(Zero-based)
     */
    var actualColIndex: Int = 0

    /**
     * The error message explaining why the cell value is not valid
     */
    lateinit var errorMessage: String

    /**
     * Updates the error message
     * A visitor implementation visits this object and decorate the error message based on locale and messageArguments
     */
    fun updateErrorMessage(visitor: CellErrorMessageFormattingVisitor) {
        errorMessage = visitor.visit(this)
    }

    /**
     * Calculate cell coordinates based on the ExcelDataRange
     */
    fun cellCoordinate(): String =
        "$actualColIndex,$actualRowIndex"

    /**
     * Calculate cell address based on the ExcelDataRange
     */
    fun cellName(): String {
        var num = actualColIndex
        val sb = StringBuilder()

        while (num >= 0) {
            val remainder = num % 26
            sb.append((remainder + 'A'.code).toChar())
            num = (num / 26) - 1
        }

        // 0   -> A
        // 1   -> B
        // 25  -> Z
        // 26  -> AA
        // 27  -> AB
        // 51  -> AZ
        // 52  -> BA
        // 701 -> ZZ
        // 702 -> AAA
        val colName = sb.reverse()

        return "$colName$actualRowIndex"
    }

    companion object {
        /**
         * Instantiates a new object from jakarta ConstraintViolation
         */
        fun of(
            rowIndex: Int,
            colIndex: Int,
            constraintViolation: ConstraintViolation<*>,
        ): CellError =
            CellError(
                rowIndex = rowIndex,
                colIndex = colIndex,
                cellConstraint = constraintViolation.constraintDescriptor.annotation.annotationClass.simpleName!!,
                messageArguments = messageArgumentsFrom(constraintViolation),
            )

        private fun messageArgumentsFrom(constraintViolation: ConstraintViolation<*>): Map<String, Any> =
            when (constraintViolation.constraintDescriptor.annotation.annotationClass) {
                Size::class -> mapOf(
                    "min" to constraintViolation.constraintDescriptor.attributes.getOrDefault("min", ""),
                    "max" to constraintViolation.constraintDescriptor.attributes.getOrDefault("max", ""),
                )
                Digits::class -> mapOf(
                    "integer" to constraintViolation.constraintDescriptor.attributes.getOrDefault("integer", ""),
                    "fraction" to constraintViolation.constraintDescriptor.attributes.getOrDefault("fraction", ""),
                )
                Pattern::class -> mapOf(
                    "regexp" to constraintViolation.constraintDescriptor.attributes.getOrDefault("regexp", ""),
                )
                else -> if (constraintViolation.constraintDescriptor.attributes.get("value") != null) {
                    mapOf(
                        "value" to constraintViolation.constraintDescriptor.attributes.getOrDefault("value", ""),
                    )
                } else {
                    emptyMap()
                }
            }
    }
}
