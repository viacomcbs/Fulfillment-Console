package com.paramount.test.ff.common.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Local paths used by the Synergy client when {@code importFile} pulls VM downloads to the runner PC.
 * Example: {@code C:\Users\<user>\AppData\Local\Temp\com.viacom.synergy\<uuid>.xlsx}
 */
public final class SynergyLocalPaths {

	private SynergyLocalPaths() {
	}

	/** {@code %LOCALAPPDATA%\Temp\com.viacom.synergy} — Synergy-staged Excel/export files on the local runner. */
	public static Path synergyImportTempDir() {
		String localAppData = System.getenv("LOCALAPPDATA");
		if (localAppData != null && !localAppData.isBlank()) {
			return Paths.get(localAppData, "Temp", "com.viacom.synergy");
		}
		return Paths.get(System.getProperty("java.io.tmpdir"), "com.viacom.synergy");
	}

	public static boolean synergyImportTempDirExists() {
		return Files.isDirectory(synergyImportTempDir());
	}
}
