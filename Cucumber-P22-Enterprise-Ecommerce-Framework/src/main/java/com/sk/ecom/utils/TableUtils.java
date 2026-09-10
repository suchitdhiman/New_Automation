package com.sk.ecom.utils;

import com.sk.ecom.driver.DriverManager;
import com.sk.ecom.exceptions.FrameworkException;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Reads an HTML table into ordinary Java collections.
 *
 * <p>Grid-heavy applications (order books, stock ledgers, pick lists) are where
 * most enterprise assertions live. Turning the DOM into
 * {@code List<Map<String,String>>} once means the assertions can be written
 * against data instead of against XPath indexes.
 */
public final class TableUtils {

	private TableUtils() {
		throw new IllegalStateException("Utility class");
	}

	/** @return the {@code th} texts, in document order. */
	public static List<String> headers(By table) {
		WebElement root = DriverManager.getDriver().findElement(table);
		return root.findElements(By.cssSelector("thead th, tr th")).stream()
				.map(WebElement::getText)
				.map(String::trim)
				.collect(Collectors.toList());
	}

	/** @return one map per body row, keyed by header text. */
	public static List<Map<String, String>> read(By table) {
		WebElement root = DriverManager.getDriver().findElement(table);
		List<String> headers = headers(table);
		if (headers.isEmpty()) {
			throw new FrameworkException("Table located by [" + table + "] has no header cells to key rows by");
		}

		List<Map<String, String>> rows = new ArrayList<>();
		for (WebElement row : root.findElements(By.cssSelector("tbody tr"))) {
			List<WebElement> cells = row.findElements(By.tagName("td"));
			if (cells.isEmpty()) {
				continue;
			}
			Map<String, String> record = new LinkedHashMap<>();
			for (int i = 0; i < cells.size() && i < headers.size(); i++) {
				record.put(headers.get(i), cells.get(i).getText().trim());
			}
			rows.add(record);
		}
		return rows;
	}

	public static int rowCount(By table) {
		return DriverManager.getDriver().findElement(table).findElements(By.cssSelector("tbody tr")).size();
	}

	public static List<String> column(By table, String header) {
		return read(table).stream().map(row -> row.getOrDefault(header, "")).collect(Collectors.toList());
	}

	/** First row where {@code header} equals {@code value}, if any. */
	public static Optional<Map<String, String>> findRow(By table, String header, String value) {
		return read(table).stream()
				.filter(row -> value.equalsIgnoreCase(row.getOrDefault(header, "")))
				.findFirst();
	}

	public static String cell(By table, String matchHeader, String matchValue, String wantedHeader) {
		return findRow(table, matchHeader, matchValue)
				.map(row -> row.get(wantedHeader))
				.orElseThrow(() -> new FrameworkException(
						"No row where [" + matchHeader + "] = [" + matchValue + "]"));
	}
}
