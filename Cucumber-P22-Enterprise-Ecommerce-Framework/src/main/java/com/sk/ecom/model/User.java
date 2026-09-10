package com.sk.ecom.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A login identity loaded from {@code testdata/users.json}.
 *
 * <p>A record rather than a bean: test data is read once and never mutated, and
 * value equality is exactly what assertions want.
 *
 * @param key         short handle used in feature files, e.g. {@code standard}
 * @param username    the credential
 * @param password    the credential
 * @param description what makes this user interesting to a tester
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record User(String key, String username, String password, String description) {

	/** Never let a password reach a log file or an HTML report. */
	@Override
	public String toString() {
		return "User[key=" + key + ", username=" + username + ", password=********]";
	}
}
