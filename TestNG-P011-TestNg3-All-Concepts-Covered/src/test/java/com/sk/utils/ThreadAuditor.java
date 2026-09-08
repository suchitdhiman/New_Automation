package com.sk.utils;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Records which thread ran which test, so the parallel demos can prove they
 * really did run in parallel instead of just claiming it in a comment.
 *
 * <p>Scoped by a caller-supplied name (normally the test class) because several
 * parallel suites share one JVM and one static map.
 *
 * <p>{@link ConcurrentHashMap} throughout - the whole point is that several
 * threads write here simultaneously.
 */
public final class ThreadAuditor {

	/** scope -> (label -> thread name) */
	private static final Map<String, Map<String, String>> ASSIGNMENTS = new ConcurrentHashMap<>();

	private ThreadAuditor() {
		// static utility
	}

	public static void record(String scope, String label) {
		ASSIGNMENTS.computeIfAbsent(scope, ignored -> new ConcurrentHashMap<>())
				.put(label, Thread.currentThread().getName());
	}

	public static Set<String> threadsUsedIn(String scope) {
		return new LinkedHashSet<>(ASSIGNMENTS.getOrDefault(scope, Map.of()).values());
	}

	public static int recordedCount(String scope) {
		return ASSIGNMENTS.getOrDefault(scope, Map.of()).size();
	}

	/** One line per label, for an {@code @AfterClass} log. */
	public static String report(String scope) {
		Map<String, String> scoped = ASSIGNMENTS.getOrDefault(scope, Map.of());
		if (scoped.isEmpty()) {
			return "(nothing recorded for " + scope + ")";
		}
		String body = scoped.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.map(entry -> "    " + entry.getKey() + "  ->  " + entry.getValue())
				.collect(Collectors.joining(System.lineSeparator()));

		return System.lineSeparator() + "  " + scope + ": " + scoped.size() + " item(s) across "
				+ threadsUsedIn(scope).size() + " thread(s)" + System.lineSeparator() + body;
	}

	public static void reset(String scope) {
		ASSIGNMENTS.remove(scope);
	}
}
