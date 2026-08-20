package com.sk.automation.exceptions;

/**
 * Unchecked exception for framework-level failures such as bad configuration or
 * an uninitialised driver.
 *
 * <p>These are defects in the harness, not failures of the application under test,
 * so they are deliberately unchecked: no test should ever try to recover from one.
 */
public class FrameworkException extends RuntimeException {

    public FrameworkException(String message) {
        super(message);
    }

    public FrameworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
