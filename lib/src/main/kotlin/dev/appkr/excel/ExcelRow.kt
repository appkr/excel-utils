package dev.appkr.excel

/**
 * Expresses a single row of an Excel
 */
interface ExcelRow {
    /**
     * Finds a (zero-based) column index that matches the given fieldName
     */
    fun getColIndexBy(fieldName: String): Int
}

data class ExcelDataRange(
    val startRow: Int,

    val startCol: Int,

    val endCol: Int,
) {
    init {
        if (listOf(startRow, startCol, endCol).any { it < 0 }) {
            throw IllegalArgumentException("Must be a positive number")
        }
        if (startCol > endCol) {
            throw IllegalArgumentException("startCol value must be smaller than endCol value")
        }
    }

    val colIndexRange: IntRange = startCol..endCol
}
