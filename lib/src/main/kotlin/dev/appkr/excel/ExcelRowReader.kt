package dev.appkr.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.primaryConstructor

open class ExcelRowReader {
    /**
     * Read single row of a Excel and creates an ExcelRow instance
     *
     * @param inputStream InputStream of an Excel
     * @param sheetIndex Zero-based
     * @param excelDataRange Data range which contains data
     * @param dataModelType Type of the class that a single Excel row will be bound
     * @throws IllegalArgumentException
     */
    fun <T : ExcelRow> read(
        inputStream: InputStream,
        sheetIndex: Int = 0,
        excelDataRange: ExcelDataRange,
        dataModelType: KClass<T>,
    ): Collection<T> {
        if (!dataModelType.isData) {
            throw IllegalArgumentException("Data class only")
        }

        val constructor = dataModelType.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor in ${dataModelType.simpleName}")

        val excelRowList = mutableListOf<T>()
        WorkbookFactory.create(inputStream)
            .use { workbook ->
                val sheet = workbook.getSheetAt(sheetIndex)

                // Loops through rows
                for (rowIndex in excelDataRange.startRow until sheet.physicalNumberOfRows) {
                    // Stops reading if encounters a blank row
                    val row = sheet.getRow(rowIndex) ?: break

                    // Loops through columns, and collects cell values
                    val rowData = mutableListOf<String>()
                    for (colIndex in excelDataRange.colIndexRange) {
                        val cell = row.getCell(colIndex)
                        rowData.add(cell?.toString() ?: "")
                    }

                    // Prepares a data map that will be bound to the constructor
                    val parameters = mutableMapOf<KParameter, Any?>()
                    for (param in constructor.parameters) {
                        val cellValue = rowData.getOrNull(param.index)
                        parameters[param] = convertCellValue(cellValue, param.type.classifier as KClass<*>)
                    }

                    excelRowList.add(constructor.callBy(parameters))
                }
            }

        return excelRowList
    }

    /**
     * Casts the value to the destination type
     *
     * @param value read from the Excel cell
     * @param targetType
     */
    protected fun convertCellValue(value: String?, targetType: KClass<*>): Any? {
        return when (targetType) {
            String::class -> value ?: ""
            Int::class -> if (value?.contains('.') == true) {
                value.toDoubleOrNull()?.toInt() ?: 0
            } else {
                value?.toIntOrNull() ?: 0
            }
            Double::class -> value?.toDoubleOrNull() ?: 0.0
            Boolean::class -> value?.toBoolean() ?: false
            else -> value
        }
    }
}
