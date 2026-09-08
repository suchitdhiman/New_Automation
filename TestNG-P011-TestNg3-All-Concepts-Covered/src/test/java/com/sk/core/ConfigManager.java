package com.sk.core;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Single place that answers "what is the value of X for this run?".
 *
 * <p>Resolution order, highest priority first:
 * <ol>
 *   <li>JVM system property, i.e. {@code -Dbrowser=edge} (surefire forwards these)</li>
 *   <li>{@code config/<env>.properties} overlay, selected by {@code -Denv=staging}</li>
 *   <li>{@code config/config.properties} base file</li>
 * </ol>
 *
 * <p>Loaded once in a static block. Everything after that is a read from an
 * already-populated snapshot, which is what makes it safe to call from
 * parallel threads without synchronising.
 */
public final class ConfigManager {

	private static final Logger LOG = LogManager.getLogger(ConfigManager.class);

	private static final String BASE_FILE = "config/config.properties";
	private static final String ENV_KEY = "env";
	private static final String DEFAULT_ENV = "qa";

	private static final Properties RESOLVED = new Properties();
	private static final String ACTIVE_ENV;

	static {
		load(BASE_FILE, true);
		ACTIVE_ENV = System.getProperty(ENV_KEY, RESOLVED.getProperty(ENV_KEY, DEFAULT_ENV)).trim();
		load("config/" + ACTIVE_ENV + ".properties", false);
		LOG.info("Configuration loaded | env={} | baseUrl={} | browser={} | headless={}",
				ACTIVE_ENV, get("base.url"), get("browser"), get("headless"));
	}

	private ConfigManager() {
		// static utility, never instantiated
	}

	private static void load(String classpathResource, boolean mandatory) {
		try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(classpathResource)) {
			if (in == null) {
				if (mandatory) {
					throw new IllegalStateException("Required config file not found on classpath: " + classpathResource);
				}
				LOG.warn("Optional config file [{}] not found - skipping overlay.", classpathResource);
				return;
			}
			Properties loaded = new Properties();
			loaded.load(in);
			// putAll so that a later overlay wins over the base file.
			RESOLVED.putAll(loaded);
		} catch (IOException e) {
			throw new IllegalStateException("Unable to read config file: " + classpathResource, e);
		}
	}

	/** @return the value for the key, never null. */
	public static String get(String key) {
		String value = System.getProperty(key);
		if (value == null || value.isBlank()) {
			value = RESOLVED.getProperty(key);
		}
		if (value == null) {
			throw new IllegalArgumentException("Missing configuration key [" + key
					+ "]. Add it to config/config.properties or pass -D" + key + "=value");
		}
		return value.trim();
	}

	public static String get(String key, String fallback) {
		String value = System.getProperty(key);
		if (value == null || value.isBlank()) {
			value = RESOLVED.getProperty(key, fallback);
		}
		return value == null ? fallback : value.trim();
	}

	public static int getInt(String key) {
		String raw = get(key);
		try {
			return Integer.parseInt(raw);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Config key [" + key + "] must be a number but was [" + raw + "]", e);
		}
	}

	public static boolean getBoolean(String key) {
		return Boolean.parseBoolean(get(key));
	}

	/** Reads an integer key expressed in seconds and hands back a Duration. */
	public static Duration getSeconds(String key) {
		return Duration.ofSeconds(getInt(key));
	}

	public static String activeEnvironment() {
		return ACTIVE_ENV;
	}

	public static String baseUrl() {
		return get("base.url");
	}
}
