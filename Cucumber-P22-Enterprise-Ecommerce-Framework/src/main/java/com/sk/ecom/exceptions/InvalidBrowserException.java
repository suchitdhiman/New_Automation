package com.sk.ecom.exceptions;

/** Thrown when an unsupported browser or execution target is requested. */
public class InvalidBrowserException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public InvalidBrowserException(String message) {
		super(message);
	}
}
