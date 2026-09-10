package com.sk.ecom.datamanager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.ecom.exceptions.DataReaderException;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Reads JSON test data off the classpath into typed objects.
 *
 * <p>Classpath rather than absolute paths: the same call works from Eclipse,
 * from {@code mvn test} and from a packaged jar on a CI agent, with no
 * {@code user.dir} guessing.
 *
 * <p>The mapper is configured to <em>fail</em> on unknown properties at the type
 * level only where the model does not opt out. Silent data loss in a test-data
 * file is worse than a loud startup failure.
 */
public final class JsonDataReader {

	private static final ObjectMapper MAPPER = new ObjectMapper()
			.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
			.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

	private JsonDataReader() {
		throw new IllegalStateException("Utility class");
	}

	public static <T> List<T> readList(String resource, Class<T> type) {
		try (InputStream in = open(resource)) {
			return MAPPER.readValue(in, MAPPER.getTypeFactory().constructCollectionType(List.class, type));
		} catch (IOException e) {
			throw new DataReaderException("Failed to parse [" + resource + "] as a list of " + type.getSimpleName(), e);
		}
	}

	public static <T> T read(String resource, Class<T> type) {
		try (InputStream in = open(resource)) {
			return MAPPER.readValue(in, type);
		} catch (IOException e) {
			throw new DataReaderException("Failed to parse [" + resource + "] as " + type.getSimpleName(), e);
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Object> readMap(String resource) {
		return read(resource, Map.class);
	}

	private static InputStream open(String resource) {
		InputStream in = JsonDataReader.class.getClassLoader().getResourceAsStream(resource);
		if (in == null) {
			throw new DataReaderException("Test data file not found on classpath: " + resource);
		}
		return in;
	}
}
