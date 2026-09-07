package com.paramount.test.ff.common.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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
		if (localAppData != null && !localAppData.trim().isEmpty()) {
			return Paths.get(localAppData, "Temp", "com.viacom.synergy");
		}
		return Paths.get(System.getProperty("java.io.tmpdir"), "com.viacom.synergy");
	}

	public static boolean synergyImportTempDirExists() {
		return Files.isDirectory(synergyImportTempDir());
	}

	/**
	 * Synergy local agent writes its active port to {@code %USERPROFILE%\Synergy\synergy_running_port.txt}.
	 * Port is dynamic (e.g. 17173) — do not hardcode 39445.
	 */
	public static Path synergyRunningPortFile() {
		return Paths.get(System.getProperty("user.home"), "Synergy", "synergy_running_port.txt");
	}

	public static String resolveLocalSynergyUrl() {
		Path portFile = synergyRunningPortFile();
		if (Files.isRegularFile(portFile)) {
			try {
				String port = new String(Files.readAllBytes(portFile), StandardCharsets.UTF_8).trim();
				if (!port.isEmpty()) {
					return "http://localhost:" + port + "/synergy";
				}
			} catch (IOException e) {
				Logger.logMessage("Could not read synergy_running_port.txt: " + e.getMessage());
			}
		}
		return "http://localhost:39445/synergy";
	}
}
