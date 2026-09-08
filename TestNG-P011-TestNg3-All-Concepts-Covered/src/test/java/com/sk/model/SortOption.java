package com.sk.model;

/**
 * The four entries of the product sort dropdown.
 *
 * <p>{@link #value} is what the {@code <option value="...">} carries and is what
 * we select by; {@link #label} is what the user sees and is what we assert on.
 * Keeping both here means a UI copy change is a one-line fix in this enum.
 */
public enum SortOption {

	NAME_A_TO_Z("az", "Name (A to Z)"),
	NAME_Z_TO_A("za", "Name (Z to A)"),
	PRICE_LOW_TO_HIGH("lohi", "Price (low to high)"),
	PRICE_HIGH_TO_LOW("hilo", "Price (high to low)");

	private final String value;
	private final String label;

	SortOption(String value, String label) {
		this.value = value;
		this.label = label;
	}

	public String value() {
		return value;
	}

	public String label() {
		return label;
	}

	public static SortOption fromValue(String value) {
		for (SortOption option : values()) {
			if (option.value.equalsIgnoreCase(value)) {
				return option;
			}
		}
		throw new IllegalArgumentException("Unknown sort option value [" + value + "]");
	}
}
