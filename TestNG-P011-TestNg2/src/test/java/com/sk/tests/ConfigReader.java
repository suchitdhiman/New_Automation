package com.sk.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * Loads every .properties file exactly once, before any test runs.
 *
 * <p>Parallel note: the maps are populated in a single static initializer and
 * are read-only afterwards, so all worker threads can share them safely.
 */
public final class ConfigReader {

	public static final String PROJECT_DIR = System.getProperty("user.dir");
	private static final String RESOURCES = PROJECT_DIR + File.separator + "src" + File.separator + "test"
			+ File.separator + "resources" + File.separator;

	private static final Properties BROWSER_PROPS = load("browser.properties");
	private static final Properties URL_PROPS = load("url.properties");
	private static final Properties OR_PROPS = load("or.properties");

	static {
		configureLog4j();
	}

	private ConfigReader() {
	}

	/** Touching this class triggers the static initializers above. */
	public static void init() {
		// no-op on purpose
	}

	public static String browser(String key) {
		return BROWSER_PROPS.getProperty(key);
	}

	/** Resolves a logical url key (amazon, facebook, bbc) to a real address. */
	public static String url(String key) {
		String value = URL_PROPS.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("No url configured for key '" + key + "' in url.properties");
		}
		return value.trim();
	}

	/** Resolves a logical locator key to its raw locator value. */
	public static String locator(String key) {
		String value = OR_PROPS.getProperty(key);
		if (value == null) {
			throw new IllegalArgumentException("No locator configured for key '" + key + "' in or.properties");
		}
		return value.trim();
	}

	private static Properties load(String fileName) {
		Properties properties = new Properties();
		try (InputStream in = new FileInputStream(new File(RESOURCES + fileName))) {
			properties.load(in);
		} catch (IOException e) {
			throw new ExceptionInInitializerError("Unable to load " + RESOURCES + fileName + " : " + e.getMessage());
		}
		return properties;
	}

	private static void configureLog4j() {
		try (InputStream in = new FileInputStream(new File(RESOURCES + "log4jConfig.properties"))) {
			Properties log4jProps = new Properties();
			log4jProps.load(in);
			PropertyConfigurator.configure(log4jProps);
		} catch (IOException e) {
			System.err.println("Could not configure log4j: " + e.getMessage());
		}
	}
}
