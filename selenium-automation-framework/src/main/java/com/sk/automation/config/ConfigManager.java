package com.sk.automation.config;

import com.sk.automation.exceptions.FrameworkException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Central read-only access to configuration.
 *
 * <p>Values are resolved in this order, first match wins:
 * <ol>
 *   <li>JVM system property  ({@code -Dbrowser=edge})</li>
 *   <li>Environment variable ({@code BROWSER=edge}) — how CI usually injects secrets</li>
 *   <li>Environment overlay  ({@code config/config-qa.properties})</li>
 *   <li>Base file            ({@code config/config.properties})</li>
 * </ol>
 *
 * <p>Files are read from the classpath, never from a hand-built absolute path, so the
 * same artefact runs identically on Windows, Linux and inside a container.
 */
public final class ConfigManager {

    private static final Logger LOG = LogManager.getLogger(ConfigManager.class);

    private static final String BASE_CONFIG = "config/config.properties";
    private static final String ENV_KEY = "env";

    private static final Properties PROPERTIES = new Properties();

    static {
        loadFromClasspath(BASE_CONFIG, true);

        String environment = System.getProperty(ENV_KEY, System.getenv("ENV"));
        if (environment != null && !environment.isBlank()) {
            loadFromClasspath("config/config-" + environment.trim() + ".properties", false);
            LOG.info("Configuration loaded for environment '{}'", environment.trim());
        }
    }

    private ConfigManager() {
        // Utility class — no instances.
    }

    private static void loadFromClasspath(String resource, boolean required) {
        try (InputStream stream = ConfigManager.class.getClassLoader().getResourceAsStream(resource)) {
            if (stream == null) {
                if (required) {
                    throw new FrameworkException("Required config file not found on classpath: " + resource);
                }
                LOG.debug("Optional config file '{}' not present — skipping", resource);
                return;
            }
            PROPERTIES.load(stream);
            LOG.debug("Loaded config file '{}'", resource);
        } catch (IOException e) {
            throw new FrameworkException("Unable to read config file: " + resource, e);
        }
    }

    /**
     * Returns the value for {@code key}, or throws if it is not defined anywhere.
     * Failing loudly at startup beats a {@code null} surfacing halfway through a suite.
     */
    public static String get(String key) {
        String value = resolve(key);
        if (value == null || value.isBlank()) {
            throw new FrameworkException("Missing configuration key: " + key);
        }
        return value.trim();
    }

    public static String get(String key, String defaultValue) {
        String value = resolve(key);
        return (value == null || value.isBlank()) ? defaultValue : value.trim();
    }

    public static int getInt(String key) {
        String value = get(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new FrameworkException("Config key '" + key + "' is not a number: " + value, e);
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return Boolean.parseBoolean(get(key, String.valueOf(defaultValue)));
    }

    private static String resolve(String key) {
        String fromSystem = System.getProperty(key);
        if (fromSystem != null && !fromSystem.isBlank()) {
            return fromSystem;
        }
        String fromEnvironment = System.getenv(key.toUpperCase().replace('.', '_'));
        if (fromEnvironment != null && !fromEnvironment.isBlank()) {
            return fromEnvironment;
        }
        return PROPERTIES.getProperty(key);
    }
}
