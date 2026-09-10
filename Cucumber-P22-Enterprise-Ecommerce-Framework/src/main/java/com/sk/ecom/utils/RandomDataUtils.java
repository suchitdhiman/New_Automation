package com.sk.ecom.utils;

import com.sk.ecom.model.CustomerInfo;

import net.datafaker.Faker;

import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates realistic, unique test data.
 *
 * <p>Hard-coded names in feature files cause two problems in a shared
 * environment: collisions between parallel threads, and green tests that only
 * pass because yesterday's record still exists. Faker-backed data avoids both.
 * Where a scenario genuinely depends on a specific value, the feature file still
 * states it explicitly — generated data is for the fields nobody is asserting on.
 */
public final class RandomDataUtils {

	private static final Faker FAKER = new Faker(Locale.US);

	private RandomDataUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String firstName() {
		return FAKER.name().firstName();
	}

	public static String lastName() {
		return FAKER.name().lastName();
	}

	public static String fullName() {
		return FAKER.name().fullName();
	}

	public static String postalCode() {
		return FAKER.address().zipCode().replaceAll("[^0-9]", "");
	}

	public static String city() {
		return FAKER.address().city();
	}

	public static String streetAddress() {
		return FAKER.address().streetAddress();
	}

	public static String phoneNumber() {
		return FAKER.phoneNumber().subscriberNumber(10);
	}

	/** Unique per call, so re-running a registration scenario never collides. */
	public static String uniqueEmail() {
		return "qa.auto." + System.currentTimeMillis() + "." + ThreadLocalRandom.current().nextInt(1000, 9999)
				+ "@example.com";
	}

	public static String companyName() {
		return FAKER.company().name();
	}

	public static String sentence() {
		return FAKER.lorem().sentence();
	}

	public static int number(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max + 1);
	}

	public static String alphanumeric(int length) {
		return FAKER.regexify("[A-Z0-9]{" + length + "}");
	}

	/** A complete, valid checkout identity in one call. */
	public static CustomerInfo customer() {
		return new CustomerInfo(firstName(), lastName(), postalCode());
	}
}
