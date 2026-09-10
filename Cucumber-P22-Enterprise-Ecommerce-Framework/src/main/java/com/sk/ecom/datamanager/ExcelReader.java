package com.sk.ecom.datamanager;

import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.exceptions.DataReaderException;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads an {@code .xlsx} sheet into {@code List<Map<String,String>>}.
 *
 * <p>Business analysts hand over spreadsheets; this is the bridge. Values come
 * back through {@link DataFormatter}, so a cell showing {@code 00123} or
 * {@code 19.90} arrives as that exact string rather than as {@code 123.0} —
 * the classic source of "works in Excel, fails in the browser".
 */
public final class ExcelReader {

	private ExcelReader() {
		throw new IllegalStateException("Utility class");
	}

	/** @param fileName file inside {@code src/test/resources/testdata} */
	public static List<Map<String, String>> read(String fileName, String sheetName) {
		File file = new File(FrameworkConstants.TEST_DATA_DIR, fileName);
		if (!file.exists()) {
			throw new DataReaderException("Excel file not found: " + file.getAbsolutePath());
		}
		try (InputStream in = new FileInputStream(file); Workbook workbook = new XSSFWorkbook(in)) {
			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				throw new DataReaderException(
						"Sheet [" + sheetName + "] not found in " + fileName + ". Available: " + sheetNames(workbook));
			}
			return toRecords(sheet);
		} catch (IOException e) {
			throw new DataReaderException("Failed to read " + file.getAbsolutePath(), e);
		}
	}

	private static List<Map<String, String>> toRecords(Sheet sheet) {
		DataFormatter formatter = new DataFormatter();
		List<Map<String, String>> records = new ArrayList<>();

		Row header = sheet.getRow(sheet.getFirstRowNum());
		if (header == null) {
			return records;
		}
		List<String> columns = new ArrayList<>();
		for (Cell cell : header) {
			columns.add(formatter.formatCellValue(cell).trim());
		}

		for (int i = sheet.getFirstRowNum() + 1; i <= sheet.getLastRowNum(); i++) {
			Row row = sheet.getRow(i);
			if (row == null) {
				continue;
			}
			Map<String, String> record = new LinkedHashMap<>();
			boolean allBlank = true;
			for (int c = 0; c < columns.size(); c++) {
				String value = formatter.formatCellValue(row.getCell(c)).trim();
				allBlank &= value.isEmpty();
				record.put(columns.get(c), value);
			}
			if (!allBlank) {
				records.add(record);
			}
		}
		return records;
	}

	private static List<String> sheetNames(Workbook workbook) {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			names.add(workbook.getSheetName(i));
		}
		return names;
	}
}
