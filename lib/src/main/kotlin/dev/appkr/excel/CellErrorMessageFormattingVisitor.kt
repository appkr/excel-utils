package dev.appkr.excel

interface CellErrorMessageFormattingVisitor {
    /**
     * Enhances error message
     */
    fun visit(cellError: CellError): String
}
