package com.sk.ecom.utils;

import com.sk.ecom.constants.FrameworkConstants;
import com.sk.ecom.exceptions.FrameworkException;
import com.sk.ecom.logging.Log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Verifies file downloads — invoices, packing slips, stock exports.
 *
 * <p>Polls the download directory instead of sleeping a fixed time, and treats a
 * {@code .crdownload}/{@code .part} temp file as "still downloading", which is
 * the usual cause of a flaky download assertion.
 */
public final class FileDownloadUtils {

	private FileDownloadUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static Path downloadDirectory() {
		Path dir = Paths.get(FrameworkConstants.DOWNLOAD_DIR);
		try {
			Files.createDirectories(dir);
		} catch (IOException e) {
			throw new FrameworkException("Cannot create download directory " + dir, e);
		}
		return dir;
	}

	/** Deletes previous artefacts so an assertion cannot pass on a stale file. */
	public static void clearDownloads() {
		File[] files = downloadDirectory().toFile().listFiles();
		if (files == null) {
			return;
		}
		Arrays.stream(files).filter(File::isFile).forEach(file -> {
			if (!file.delete()) {
				Log.warn("Could not delete stale download: " + file.getName());
			}
		});
	}

	/**
	 * Waits until a fully written file whose name contains {@code fragment}
	 * appears.
	 *
	 * @throws FrameworkException if nothing arrives within {@code timeout}
	 */
	public static Path awaitDownload(String fragment, Duration timeout) {
		Instant deadline = Instant.now().plus(timeout);
		while (Instant.now().isBefore(deadline)) {
			Optional<Path> match = findCompleted(fragment);
			if (match.isPresent()) {
				Log.step("Downloaded file detected: %s", match.get().getFileName());
				return match.get();
			}
		}
		throw new FrameworkException("No completed download containing [" + fragment + "] appeared in "
				+ downloadDirectory() + " within " + timeout.toSeconds() + "s");
	}

	public static Path awaitDownload(String fragment) {
		return awaitDownload(fragment, Duration.ofSeconds(30));
	}

	private static Optional<Path> findCompleted(String fragment) {
		File[] files = downloadDirectory().toFile().listFiles();
		if (files == null) {
			return Optional.empty();
		}
		return Arrays.stream(files)
				.filter(File::isFile)
				.filter(file -> file.getName().toLowerCase().contains(fragment.toLowerCase()))
				.filter(file -> !file.getName().endsWith(".crdownload") && !file.getName().endsWith(".part"))
				.filter(file -> file.length() > 0)
				.map(File::toPath)
				.findFirst();
	}

	public static boolean exists(String fragment) {
		return findCompleted(fragment).isPresent();
	}

	public static long sizeOf(Path file) {
		try {
			return Files.size(Objects.requireNonNull(file));
		} catch (IOException e) {
			throw new FrameworkException("Cannot read size of " + file, e);
		}
	}
}
