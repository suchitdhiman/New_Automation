package com.sk.ecom.exceptions;

/** Thrown when a configuration file is missing or a required key is absent. */
public class ConfigurationException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public ConfigurationException(String message) {
		super(message);
	}

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}
}
