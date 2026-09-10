package com.sk.ecom.datamanager;

import com.sk.ecom.exceptions.DataReaderException;
import com.sk.ecom.model.Product;
import com.sk.ecom.model.User;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Read-through cache over the test-data files.
 *
 * <p>Files are parsed once per JVM and served from immutable maps, so a suite of
 * 300 scenarios does not re-read {@code users.json} 300 times, and no scenario
 * can mutate data another thread is about to read.
 *
 * <p>Feature files refer to users by their <em>key</em> ({@code standard},
 * {@code locked_out}), never by raw credentials — so rotating a password is a
 * one-line change in JSON rather than a find-and-replace across Gherkin.
 */
public final class TestDataProvider {

	private static final String USERS_FILE = "testdata/users.json";
	private static final String PRODUCTS_FILE = "testdata/products.json";

	private static final class Holder {
		static final Map<String, User> USERS = JsonDataReader.readList(USERS_FILE, User.class).stream()
				.collect(Collectors.toUnmodifiableMap(User::key, Function.identity()));
		static final List<Product> PRODUCTS = List.copyOf(JsonDataReader.readList(PRODUCTS_FILE, Product.class));
	}

	private TestDataProvider() {
		throw new IllegalStateException("Utility class");
	}

	/**
	 * @param key the handle used in feature files
	 * @throws DataReaderException listing the valid keys, so a typo in Gherkin
	 *                             produces a useful message instead of an NPE
	 */
	public static User user(String key) {
		User user = Holder.USERS.get(key.trim().toLowerCase());
		if (user == null) {
			throw new DataReaderException(
					"No user with key [" + key + "] in " + USERS_FILE + ". Known keys: " + Holder.USERS.keySet());
		}
		return user;
	}

	public static List<User> allUsers() {
		return List.copyOf(Holder.USERS.values());
	}

	public static List<Product> products() {
		return Holder.PRODUCTS;
	}

	public static Product product(String name) {
		return Holder.PRODUCTS.stream()
				.filter(p -> p.name().equalsIgnoreCase(name.trim()))
				.findFirst()
				.orElseThrow(() -> new DataReaderException("No product named [" + name + "] in " + PRODUCTS_FILE));
	}
}
