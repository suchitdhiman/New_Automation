package com.sk.ecom.config;

import com.sk.ecom.enums.ConfigKey;
import com.sk.ecom.exceptions.ConfigurationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;

/**
 * Single source of truth for configuration, with a deliberate override order.
 *
 * <pre>
 *   1. -D system property        (highest — what CI and the IDE pass in)
 *   2. OS environment variable   (APP_BASE_URL for app.base.url)
 *   3. config/&lt;env&gt;.properties   (qa / staging / prod overlay)
 *   4. config/config.properties  (lowest — the checked-in defaults)
 * </pre>
 *
 * <p>Loaded once in a static initialiser, so it is inherently thread safe: by
 * the time any scenario thread reads a key the {@link Properties} instance is
 * fully populated and never mutated again.
 */
public final class ConfigManager {

	private static final String BASE_FILE = "config/config.properties";
	private static final Properties PROPERTIES = new Properties();
	private static final String ENVIRONMENT;

	static {
		loadFromClasspath(BASE_FILE, true);
		ENVIRONMENT = resolveEnvironment();
		loadFromClasspath("config/" + ENVIRONMENT + ".properties", false);
	}

	private ConfigManager() {
		throw new IllegalStateException("Utility class");
	}

	/* ------------------------------------------------------------------ */
	/* Loading                                                             */
	/* ------------------------------------------------------------------ */

	private static void loadFromClasspath(String resource, boolean mandatory) {
		try (InputStream in = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
			if (in == null) {
				if (mandatory) {
					throw new ConfigurationException("Mandatory config file not found on classpath: " + resource);
				}
				return;
			}
			PROPERTIES.load(in);
		} catch (IOException e) {
			throw new ConfigurationException("Unable to read config file: " + resource, e);
		}
	}

	private static String resolveEnvironment() {
		return Optional.ofNullable(System.getProperty(ConfigKey.ENV.key()))
				.or(() -> Optional.ofNullable(System.getenv("ENV")))
				.or(() -> Optional.ofNullable(PROPERTIES.getProperty(ConfigKey.ENV.key())))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.orElse("qa")
				.toLowerCase(Locale.ROOT);
	}

	/* ------------------------------------------------------------------ */
	/* Reading                                                             */
	/* ------------------------------------------------------------------ */

	/** @return the active environment name, e.g. {@code qa}. */
	public static String environment() {
		return ENVIRONMENT;
	}

	/**
	 * @throws ConfigurationException when the key is defined nowhere. A missing
	 *                                key is a setup bug, not a test failure, so
	 *                                it must not be silently defaulted.
	 */
	public static String get(ConfigKey key) {
		return lookup(key.key()).orElseThrow(() -> new ConfigurationException(
				"Missing configuration key [" + key.key() + "]. Add it to " + BASE_FILE
						+ " or pass -D" + key.key() + "=<value>"));
	}

	public static String get(ConfigKey key, String defaultValue) {
		return lookup(key.key()).orElse(defaultValue);
	}

	public static int getInt(ConfigKey key) {
		String raw = get(key);
		try {
			return Integer.parseInt(raw.trim());
		} catch (NumberFormatException e) {
			throw new ConfigurationException("Config key [" + key.key() + "] must be a number but was [" + raw + "]", e);
		}
	}

	public static int getInt(ConfigKey key, int defaultValue) {
		return lookup(key.key()).map(String::trim).map(Integer::parseInt).orElse(defaultValue);
	}

	public static boolean getBoolean(ConfigKey key) {
		return Boolean.parseBoolean(get(key, "false").trim());
	}

	/** Escape hatch for ad-hoc keys that do not deserve a {@link ConfigKey}. */
	public static String getRaw(String key, String defaultValue) {
		return lookup(key).orElse(defaultValue);
	}

	private static Optional<String> lookup(String key) {
		return Optional.ofNullable(System.getProperty(key))
				.or(() -> Optional.ofNullable(System.getenv(toEnvVar(key))))
				.or(() -> Optional.ofNullable(PROPERTIES.getProperty(key)))
				.map(String::trim)
				.filter(value -> !value.isEmpty());
	}

	/** {@code app.base.url} becomes {@code APP_BASE_URL}. */
	private static String toEnvVar(String key) {
		return key.toUpperCase(Locale.ROOT).replace('.', '_');
	}
}
