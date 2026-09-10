package com.sk.ecom.exceptions;

/** Thrown when a JSON / Excel / CSV test-data source cannot be read. */
public class DataReaderException extends FrameworkException {

	private static final long serialVersionUID = 1L;

	public DataReaderException(String message, Throwable cause) {
		super(message, cause);
	}

	public DataReaderException(String message) {
		super(message);
	}
}
