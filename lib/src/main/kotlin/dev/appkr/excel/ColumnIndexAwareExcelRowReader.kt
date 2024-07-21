package dev.appkr.excel

import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ExcelColumnIndex(val value: Int)

/**
 * Expresses a single row of an Excel
 */
abstract class ColumnIndexAnnotatedExcelRow : ExcelRow {
    val colIndexMap: Map<String, Int> by lazy {
        getColIndexMapOf(this::class)
            .map { (fieldName, annotation) -> fieldName to annotation.value }
            .toMap()
    }

    /**
     * Finds a (zero-based) column index based on the @ExcelColumnIndex annotation's value
     */
    override fun getColIndexBy(fieldName: String): Int {
        return colIndexMap[fieldName]
            ?: throw IllegalArgumentException("${ExcelColumnIndex::class.simpleName} annotation was not found on $fieldName")
    }
}

/**
 * Helpers to collect pairs of fieldName and
 *
 * @param dataModelType
 * @return e.g. {name=@ExcelColumnIndex(1), age=@ExcelColumnIndex(2)}
 */
internal fun <T : ColumnIndexAnnotatedExcelRow> getColIndexMapOf(
    dataModelType: KClass<T>,
): Map<String, ExcelColumnIndex> {
    val colIndexMap = dataModelType.memberProperties
        .mapNotNull { prop ->
            val annotation = prop.findAnnotation<ExcelColumnIndex>() ?: return@mapNotNull null
            prop.name to annotation
        }
        .toMap()

    if (colIndexMap.isEmpty()) {
        throw IllegalArgumentException(
            "${dataModelType.simpleName} 클래스에서 ${ExcelColumnIndex::class.simpleName} 애너테이션이 누락됐습니다",
        )
    }

    return colIndexMap
}

class ColumnIndexAwareExcelRowReader : ExcelRowReader() {
    /**
     * Read single row of a Excel and creates an ColumnIndexAnnotatedExcelRow instance
     *
     * @param inputStream InputStream of an Excel
     * @param sheetIndex Zero-based
     * @param startRow Zero-based
     * @param dataModelType Type of the class that a single Excel row will be bound
     * @throws IllegalArgumentException
     * @throws IllegalArgumentException
     */
    fun <T : ColumnIndexAnnotatedExcelRow> read(
        inputStream: InputStream,
        sheetIndex: Int = 0,
        startRow: Int = 0,
        dataModelType: KClass<T>,
    ): Collection<T> {
        if (!dataModelType.isData) {
            throw IllegalArgumentException("Data class only")
        }

        val constructor = dataModelType.primaryConstructor
            ?: throw IllegalArgumentException("No primary constructor in ${dataModelType.simpleName}")

        val colIndexMap = getColIndexMapOf(dataModelType)

        val excelRowList = mutableListOf<T>()
        WorkbookFactory.create(inputStream).use { workbook ->
            val sheet = workbook.getSheetAt(sheetIndex)

            // Loops through rows
            for (rowIndex in startRow until sheet.physicalNumberOfRows) {
                // Stops reading if encounters a blank row
                val row = sheet.getRow(rowIndex) ?: break

                // Loops through columns, and collects cell values
                val parameters = mutableMapOf<KParameter, Any?>()
                for (param: KParameter in constructor.parameters) {
                    val annotation = colIndexMap.getValue(param.name!!)
                    val cell = row.getCell(annotation.value)
                    parameters[param] = convertCellValue(cell?.toString() ?: "", param.type.classifier as KClass<*>)
                }

                excelRowList.add(constructor.callBy(parameters))
            }
        }

        return excelRowList
    }
}
