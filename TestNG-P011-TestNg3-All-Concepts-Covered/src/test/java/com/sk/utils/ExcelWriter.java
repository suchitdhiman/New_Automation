package com.sk.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.sk.listeners.ExecutionLedger.TestOutcome;

/**
 * Writes the run back out to a spreadsheet, colour coded by status.
 *
 * <p>Called exactly once, from the {@code IReporter} at the end of the run, on
 * a single thread. That is deliberate: POI workbooks are not thread safe, so
 * having twenty parallel tests each open and save the same file would corrupt
 * it. Results are collected in {@code ExecutionLedger} while the suite runs and
 * flushed here.
 */
public final class ExcelWriter {

	private static final Logger LOG = LogManager.getLogger(ExcelWriter.class);

	private static final String[] HEADERS = {
			"#", "Suite", "Test (xml)", "Class", "Method", "Parameters",
			"Status", "Duration (ms)", "Retries", "Thread", "Failure reason" };

	private ExcelWriter() {
		// static utility
	}

	public static void writeResults(String targetPath, List<TestOutcome> outcomes) {
		Path target = Paths.get(targetPath);

		try (Workbook workbook = new XSSFWorkbook()) {
			Sheet sheet = workbook.createSheet("Execution Results");

			CellStyle headerStyle = headerStyle(workbook);
			Row header = sheet.createRow(0);
			for (int i = 0; i < HEADERS.length; i++) {
				Cell cell = header.createCell(i);
				cell.setCellValue(HEADERS[i]);
				cell.setCellStyle(headerStyle);
			}

			CellStyle pass = statusStyle(workbook, IndexedColors.LIGHT_GREEN);
			CellStyle fail = statusStyle(workbook, IndexedColors.ROSE);
			CellStyle skip = statusStyle(workbook, IndexedColors.LEMON_CHIFFON);

			int rowIndex = 1;
			for (TestOutcome outcome : outcomes) {
				Row row = sheet.createRow(rowIndex);
				row.createCell(0).setCellValue(rowIndex);
				row.createCell(1).setCellValue(outcome.suiteName());
				row.createCell(2).setCellValue(outcome.xmlTestName());
				row.createCell(3).setCellValue(outcome.className());
				row.createCell(4).setCellValue(outcome.methodName());
				row.createCell(5).setCellValue(outcome.parameters());

				Cell statusCell = row.createCell(6);
				statusCell.setCellValue(outcome.status().name());
				statusCell.setCellStyle(switch (outcome.status()) {
					case PASSED -> pass;
					case FAILED -> fail;
					case SKIPPED -> skip;
				});

				row.createCell(7).setCellValue(outcome.durationMs());
				row.createCell(8).setCellValue(outcome.retryCount());
				row.createCell(9).setCellValue(outcome.threadName());
				row.createCell(10).setCellValue(truncate(outcome.failureReason()));
				rowIndex++;
			}

			for (int i = 0; i < HEADERS.length; i++) {
				sheet.autoSizeColumn(i);
			}
			sheet.createFreezePane(0, 1);

			if (target.getParent() != null) {
				Files.createDirectories(target.getParent());
			}
			try (OutputStream out = new FileOutputStream(target.toFile())) {
				workbook.write(out);
			}
			LOG.info("Excel results written: {}", target.toAbsolutePath());

		} catch (IOException e) {
			// Never fail the build over a report file.
			LOG.error("Could not write the Excel result file [{}]: {}", target, e.getMessage());
		}
	}

	private static CellStyle headerStyle(Workbook workbook) {
		Font font = workbook.createFont();
		font.setBold(true);
		font.setColor(IndexedColors.WHITE.getIndex());

		CellStyle style = workbook.createCellStyle();
		style.setFont(font);
		style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	private static CellStyle statusStyle(Workbook workbook, IndexedColors colour) {
		CellStyle style = workbook.createCellStyle();
		style.setFillForegroundColor(colour.getIndex());
		style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		return style;
	}

	/** Excel refuses any cell text longer than 32767 characters; stack traces get close. */
	private static String truncate(String value) {
		if (value == null) {
			return "";
		}
		return value.length() > 3000 ? value.substring(0, 3000) + " ...[truncated]" : value;
	}
}
