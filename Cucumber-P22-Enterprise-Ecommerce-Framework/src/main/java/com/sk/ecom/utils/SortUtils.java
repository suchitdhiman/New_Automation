package com.sk.ecom.utils;

import java.util.Comparator;
import java.util.List;

/**
 * Order assertions for catalogue and grid screens.
 *
 * <p>Written as pure functions over lists so they can be unit tested and so the
 * step definitions read as {@code assertThat(SortUtils.isAscending(prices))}
 * instead of hand-rolling a loop in every sorting scenario.
 */
public final class SortUtils {

	private SortUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static <T extends Comparable<T>> boolean isAscending(List<T> values) {
		return isSorted(values, Comparator.naturalOrder());
	}

	public static <T extends Comparable<T>> boolean isDescending(List<T> values) {
		return isSorted(values, Comparator.reverseOrder());
	}

	public static <T> boolean isSorted(List<T> values, Comparator<T> comparator) {
		for (int i = 0; i < values.size() - 1; i++) {
			if (comparator.compare(values.get(i), values.get(i + 1)) > 0) {
				return false;
			}
		}
		return true;
	}

	/** Case-insensitive A→Z, which is what a product name sort actually means. */
	public static boolean isAlphabeticalAscending(List<String> values) {
		return isSorted(values, String.CASE_INSENSITIVE_ORDER);
	}

	public static boolean isAlphabeticalDescending(List<String> values) {
		return isSorted(values, String.CASE_INSENSITIVE_ORDER.reversed());
	}
}
