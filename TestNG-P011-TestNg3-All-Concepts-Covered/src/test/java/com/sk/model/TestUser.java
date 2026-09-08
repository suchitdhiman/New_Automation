package com.sk.model;

import com.sk.core.ConfigManager;

/**
 * The application accounts we test with.
 *
 * <p>Swag Labs publishes these credentials on its own login page, so there is
 * nothing secret here - but they still live in {@code config.properties} rather
 * than being hard-coded, because that is the habit you want when the same
 * framework points at a real environment.
 *
 * <p>Each account behaves differently on purpose, which is what makes this site
 * useful for demonstrating retries, timeouts and soft assertions:
 * <ul>
 *   <li>{@link #STANDARD} - the happy path.</li>
 *   <li>{@link #LOCKED} - always rejected at login.</li>
 *   <li>{@link #PROBLEM} - logs in, then renders broken data (wrong images, sort does nothing).</li>
 *   <li>{@link #PERFORMANCE} - logs in successfully but very slowly.</li>
 * </ul>
 */
public enum TestUser {

	STANDARD("user.standard"),
	LOCKED("user.locked"),
	PROBLEM("user.problem"),
	PERFORMANCE("user.performance");

	private final String usernameKey;

	TestUser(String usernameKey) {
		this.usernameKey = usernameKey;
	}

	public String username() {
		return ConfigManager.get(usernameKey);
	}

	public String password() {
		return ConfigManager.get("user.password");
	}
}
