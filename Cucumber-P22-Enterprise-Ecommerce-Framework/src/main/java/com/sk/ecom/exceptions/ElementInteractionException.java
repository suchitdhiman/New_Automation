package com.sk.ecom.exceptions;

import org.openqa.selenium.By;

/**
 * Thrown when an interaction with an element fails after the framework has
 * exhausted its waits and its JavaScript fallback. The message always carries
 * the business name of the element so the report reads like English rather than
 * like a stack trace.
 */
public class ElementInteractionException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public ElementInteractionException(String action, String elementName, By locator, Throwable cause) {
		super(String.format("Failed to %s on [%s] located by [%s]", action, elementName, locator), cause);
	}

	public ElementInteractionException(String message, Throwable cause) {
		super(message, cause);
	}
}
