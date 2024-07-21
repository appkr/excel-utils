package dev.appkr.excel

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import kotlin.math.min

/**
 * Removes cell formatting and comment for the given cell range
 */
fun Sheet.clearErrors(firstRow: Int, firstCol: Int, lastCol: Int, lastRow: Int = Int.MAX_VALUE) {
    (firstRow..min(lastRow, lastRowNum))
        .forEach outer@{ rowNum ->
            val row = getRow(rowNum) ?: return@outer
            (firstCol..lastCol)
                .forEach { colNum ->
                    val cell = row.getCell(colNum) ?: return@forEach

                    // Removes foreground color
                    cell.cellStyle
                        .apply {
                            fillForegroundColor = IndexedColors.AUTOMATIC.index
                            fillPattern = FillPatternType.NO_FILL
                        }

                    // Removes cell comment
                    cell.removeCellComment()
                }
        }
}

/**
 * Applies cell formatting and comment against the cell given by CellError
 */
fun Sheet.markError(cellError: CellError) {
    // Finds the cell
    val cell = getRow(cellError.actualRowIndex)?.getCell(cellError.actualColIndex) ?: return

    // More than one cell comments are not allowed, So,
    // Caching previous cell comments for future concatenation
    val messageBin = if (cell.cellComment != null) {
        listOf(cell.cellComment.string.toString())
    } else {
        listOf(cellError.errorMessage)
    }

    // Removes existing cell comment
    if (cell.cellComment != null) cell.removeCellComment()

    // Draws a comment box
    val anchor = workbook.creationHelper.createClientAnchor()
        .apply {
            // Places the comment box at plus 2 cells horizontally and 1 cell vertically from the current cell
            setCol1(cellError.actualColIndex)
            setCol2(cellError.actualColIndex + 2)
            row1 = cellError.actualRowIndex
            row2 = cellError.actualRowIndex + 1
        }

    // Fills content of the comment box
    cell.cellComment = createDrawingPatriarch().createCellComment(anchor)
        .apply {
            string = workbook.creationHelper.createRichTextString(messageBin.joinToString { it })
            author = "YOUR NAME" // TODO extract
        }

    // Applies error styles
    val cellStyle = workbook.createCellStyle()
    cellStyle.cloneStyleFrom(cell.cellStyle)
    cell.cellStyle = cellStyle
        .apply {
            fillForegroundColor = IndexedColors.LEMON_CHIFFON.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
        }
}

/**
 * Finds out where there are blank row in between
 *
 * e.g.
 * Given 0~999 row range, if 0th row is blank then we way blank
 * Given 0~999 row range, if only 0th row is not blank, then we way it's not blank
 * Given 0~999 row range, if from 0th to 9th are not blank, but 10th is blank, and then 11th are not blank, then we say blank
 * Given 0~999 row range, if from 0th to 9th are not blank, then all the way to 998th row are blank, but 999th row are not blank, then we say blank
 */
fun Sheet.hasAnyBlankRows(firstRow: Int, firstCol: Int, lastCol: Int, lastRow: Int = Int.MAX_VALUE): Boolean {
    if (getRow(firstRow)?.isBlank(firstCol, lastCol) == true) {
        return true
    }

    var foundNonBlankRow = false
    var foundBlankRowAfterNonBlank = false
    (firstRow + 1..min(lastRow, lastRowNum))
        .forEach { rowIndex ->
            val row = getRow(rowIndex)
            if (row == null || row.isBlank(firstCol, lastCol)) {
                if (foundNonBlankRow) {
                    foundBlankRowAfterNonBlank = true
                }
            } else {
                if (foundBlankRowAfterNonBlank) {
                    return true
                }
                foundNonBlankRow = true
            }
        }

    return false
}

private fun Row.isBlank(firstCol: Int, lastCol: Int): Boolean =
    (firstCol..lastCol)
        .mapNotNull { getCell(it) }
        .all { it.isBlank() }

private fun Cell.isBlank(): Boolean =
    when (cellType) {
        CellType.BLANK -> true
        CellType.STRING -> stringCellValue.isNullOrBlank()
        // NUMERIC, BOOLEAN, FORMULA are always not blank
        else -> false
    }
