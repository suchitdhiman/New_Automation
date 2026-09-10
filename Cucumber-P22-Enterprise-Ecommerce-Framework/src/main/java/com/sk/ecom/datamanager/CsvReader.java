package com.sk.ecom.datamanager;

import com.sk.ecom.exceptions.DataReaderException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Minimal CSV reader for classpath test data.
 *
 * <p>Handles quoted fields and embedded commas, which is the only part of CSV
 * that people actually get wrong. Anything more exotic than that belongs in a
 * real CSV library — but for checked-in test data this keeps the dependency
 * list shorter.
 */
public final class CsvReader {

	private CsvReader() {
		throw new IllegalStateException("Utility class");
	}

	public static List<Map<String, String>> read(String resource) {
		try (InputStream in = CsvReader.class.getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				throw new DataReaderException("CSV file not found on classpath: " + resource);
			}
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
				String headerLine = reader.readLine();
				if (headerLine == null) {
					return List.of();
				}
				List<String> headers = splitLine(headerLine);
				List<Map<String, String>> rows = new ArrayList<>();
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isBlank()) {
						continue;
					}
					List<String> values = splitLine(line);
					Map<String, String> row = new LinkedHashMap<>();
					for (int i = 0; i < headers.size(); i++) {
						row.put(headers.get(i), i < values.size() ? values.get(i) : "");
					}
					rows.add(row);
				}
				return rows;
			}
		} catch (IOException e) {
			throw new DataReaderException("Failed to read CSV [" + resource + "]", e);
		}
	}

	private static List<String> splitLine(String line) {
		List<String> fields = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		boolean inQuotes = false;

		for (int i = 0; i < line.length(); i++) {
			char c = line.charAt(i);
			if (c == '"') {
				// A doubled quote inside a quoted field is a literal quote.
				if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
					current.append('"');
					i++;
				} else {
					inQuotes = !inQuotes;
				}
			} else if (c == ',' && !inQuotes) {
				fields.add(current.toString().trim());
				current.setLength(0);
			} else {
				current.append(c);
			}
		}
		fields.add(current.toString().trim());
		return fields;
	}
}
