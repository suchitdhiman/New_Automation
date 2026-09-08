package com.sk.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * Reads an .xlsx sheet into row maps keyed by the header text.
 *
 * <h2>Why maps and not Object[][] straight away</h2>
 * A DataProvider that returns raw {@code Object[][]} pins the test to column
 * <em>positions</em>. Someone inserts a column in the spreadsheet six months
 * later and thirty tests start comparing a postcode against a surname. Reading
 * into {@code Map<header, value>} and then building typed objects means an
 * inserted column changes nothing, and a <em>renamed</em> column fails loudly
 * in {@link #require(java.util.Map, String)} instead of silently.
 *
 * <h2>Why DataFormatter</h2>
 * {@code cell.getStringCellValue()} throws on a numeric cell, and
 * {@code getNumericCellValue()} turns the postcode 12345 into "12345.0".
 * {@link DataFormatter} gives back exactly what Excel displays, for every cell
 * type including formulas, which is what a test data sheet actually wants.
 *
 * <p>The workbook is opened, drained into memory and closed inside each call.
 * Read once at DataProvider time, hand the rows out, hold no file handle -
 * POI workbooks are not thread safe and DataProviders can run in parallel.
 */
public final class ExcelReader {

	private static final Logger LOG = LogManager.getLogger(ExcelReader.class);
	private static final DataFormatter FORMATTER = new DataFormatter();

	private ExcelReader() {
		// static utility
	}

	/**
	 * @return one {@code Map<header, cellText>} per data row, header row excluded.
	 *         Fully blank rows are dropped, which is what you want when someone
	 *         has hit delete on a row instead of removing it.
	 */
	public static List<Map<String, String>> read(String filePath, String sheetName) {
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			throw new IllegalStateException("Test data workbook not found: " + path.toAbsolutePath()
					+ ". Check excel.testdata.path in config.properties.");
		}

		List<Map<String, String>> rows = new ArrayList<>();

		try (InputStream in = new FileInputStream(path.toFile());
				Workbook workbook = new XSSFWorkbook(in)) {

			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet [" + sheetName + "] is not in " + path.getFileName()
						+ ". Available sheets: " + sheetNames(workbook));
			}

			Row headerRow = sheet.getRow(sheet.getFirstRowNum());
			if (headerRow == null) {
				throw new IllegalStateException("Sheet [" + sheetName + "] has no header row.");
			}
			List<String> headers = new ArrayList<>();
			for (int c = 0; c < headerRow.getLastCellNum(); c++) {
				headers.add(cellText(headerRow.getCell(c)));
			}

			for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
				Row row = sheet.getRow(r);
				if (row == null) {
					continue;
				}
				Map<String, String> values = new LinkedHashMap<>();
				boolean anyValue = false;
				for (int c = 0; c < headers.size(); c++) {
					String header = headers.get(c);
					if (header.isBlank()) {
						continue;
					}
					String value = cellText(row.getCell(c));
					values.put(header, value);
					anyValue = anyValue || !value.isBlank();
				}
				if (anyValue) {
					// Excel row numbers are 1-based; keep the real one for error messages.
					values.put(ROW_NUMBER, String.valueOf(r + 1));
					rows.add(values);
				}
			}
		} catch (IOException e) {
			throw new IllegalStateException("Failed to read " + path.toAbsolutePath(), e);
		}

		LOG.info("Read {} data row(s) from sheet [{}] of {}", rows.size(), sheetName, path.getFileName());
		return rows;
	}

	/** Synthetic column added to every row so failures can point at the spreadsheet line. */
	public static final String ROW_NUMBER = "__excelRow";

	/** Reads a mandatory column and complains with the sheet coordinates when it is missing. */
	public static String require(Map<String, String> row, String column) {
		String value = row.get(column);
		if (value == null) {
			throw new IllegalArgumentException("Column [" + column + "] is missing (row " + row.get(ROW_NUMBER)
					+ "). Columns present: " + row.keySet());
		}
		return value.trim();
	}

	public static String optional(Map<String, String> row, String column, String fallback) {
		String value = row.get(column);
		return (value == null || value.isBlank()) ? fallback : value.trim();
	}

	/** Accepts Y / YES / TRUE / 1 in either case, everything else is false. */
	public static boolean flag(Map<String, String> row, String column) {
		String value = optional(row, column, "N").toUpperCase();
		return value.equals("Y") || value.equals("YES") || value.equals("TRUE") || value.equals("1");
	}

	private static String cellText(Cell cell) {
		return cell == null ? "" : FORMATTER.formatCellValue(cell).trim();
	}

	private static List<String> sheetNames(Workbook workbook) {
		List<String> names = new ArrayList<>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			names.add(workbook.getSheetName(i));
		}
		return names;
	}
}
