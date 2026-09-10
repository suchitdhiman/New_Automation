package com.sk.ecom.utils;

import com.sk.ecom.exceptions.FrameworkException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Money parsing and arithmetic.
 *
 * <p>{@link BigDecimal} throughout, never {@code double}: a checkout total
 * assertion done in floating point fails intermittently on values like
 * {@code 0.1 + 0.2}, and an intermittent failure in a money assertion is the
 * fastest way to lose trust in a suite.
 */
public final class PriceUtils {

	private PriceUtils() {
		throw new IllegalStateException("Utility class");
	}

	/** Strips currency symbols, labels and thousands separators: {@code "Total: $49.99" -> 49.99}. */
	public static BigDecimal parse(String raw) {
		if (raw == null || raw.isBlank()) {
			throw new FrameworkException("Cannot parse a price from an empty string");
		}
		String digits = raw.replaceAll("[^0-9.\\-]", "");
		if (digits.isBlank()) {
			throw new FrameworkException("No numeric value found in [" + raw + "]");
		}
		try {
			return new BigDecimal(digits).setScale(2, RoundingMode.HALF_UP);
		} catch (NumberFormatException e) {
			throw new FrameworkException("Cannot parse a price from [" + raw + "]", e);
		}
	}

	public static List<BigDecimal> parseAll(List<String> raw) {
		return raw.stream().map(PriceUtils::parse).toList();
	}

	public static BigDecimal sum(List<BigDecimal> values) {
		return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
	}

	/** @return {@code amount * rate}, rounded half-up to cents. */
	public static BigDecimal percentageOf(BigDecimal amount, BigDecimal rate) {
		return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
	}

	public static BigDecimal of(String plain) {
		return new BigDecimal(plain).setScale(2, RoundingMode.HALF_UP);
	}

	/** Compares by value, so {@code 10.0} and {@code 10.00} are equal. */
	public static boolean equal(BigDecimal left, BigDecimal right) {
		return left.compareTo(right) == 0;
	}

	public static String format(BigDecimal amount) {
		return "$" + amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}
}
