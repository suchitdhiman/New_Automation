package com.sk.ecom.exceptions;

/**
 * Root of every exception the framework throws on purpose.
 *
 * <p>Unchecked by design: a test that cannot continue should fail fast and loud
 * rather than force every caller to declare {@code throws}. Catch this type in
 * hooks/listeners when you need to convert a framework failure into a report
 * entry.
 */
public class FrameworkException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public FrameworkException(String message) {
		super(message);
	}

	public FrameworkException(String message, Throwable cause) {
		super(message, cause);
	}
}
