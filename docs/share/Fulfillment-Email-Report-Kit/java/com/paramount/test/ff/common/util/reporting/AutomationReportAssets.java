package com.paramount.test.ff.common.util.reporting;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

/**
 * Classpath assets embedded in automation report emails.
 */
public final class AutomationReportAssets {

    public static final String PARAMOUNT_LOGO_CID = "paramount-logo@automation-report";
    private static final String PARAMOUNT_LOGO_RESOURCE = "/reporting/paramount-logo.png";
    /** Square navy badge; mountain/stars centered in the asset. */
    public static final int PARAMOUNT_LOGO_DISPLAY_HEIGHT_PX = 44;
    public static final int PARAMOUNT_LOGO_DISPLAY_WIDTH_PX = 44;

    private static byte[] paramountLogoBytes;
    private static String paramountLogoDataUri;

    private AutomationReportAssets() {
    }

    public static String paramountLogoDataUri() {
        if (paramountLogoDataUri == null) {
            byte[] bytes = paramountLogoBytes();
            if (bytes.length == 0) {
                paramountLogoDataUri = "";
            } else {
                paramountLogoDataUri = "data:image/png;base64," + Base64.getEncoder().encodeToString(bytes);
            }
        }
        return paramountLogoDataUri;
    }

    public static byte[] paramountLogoBytes() {
        if (paramountLogoBytes == null) {
            paramountLogoBytes = loadResourceBytes(PARAMOUNT_LOGO_RESOURCE);
        }
        return paramountLogoBytes;
    }

    public static boolean hasParamountLogo() {
        return paramountLogoBytes().length > 0;
    }

    /**
     * Writes the logo to a temp file for Synergy upload or MIME inline attachment.
     */
    public static Path writeParamountLogoTempFile() {
        return writeParamountLogoTempFile("default");
    }

    public static Path writeParamountLogoTempFile(String versionTag) {
        byte[] bytes = paramountLogoBytes();
        if (bytes.length == 0) {
            return null;
        }
        try {
            String suffix = versionTag == null || versionTag.isBlank() ? ".png" : "-" + versionTag + ".png";
            Path temp = Files.createTempFile("paramount-logo", suffix);
            Files.write(temp, bytes);
            temp.toFile().deleteOnExit();
            return temp;
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] loadResourceBytes(String resourcePath) {
        try (InputStream in = openResourceStream(resourcePath)) {
            if (in == null) {
                return new byte[0];
            }
            return in.readAllBytes();
        } catch (Exception e) {
            return new byte[0];
        }
    }

    private static InputStream openResourceStream(String resourcePath) {
        InputStream in = AutomationReportAssets.class.getResourceAsStream(resourcePath);
        if (in != null) {
            return in;
        }
        String withoutLeadingSlash = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader != null) {
            in = contextLoader.getResourceAsStream(withoutLeadingSlash);
            if (in != null) {
                return in;
            }
        }
        return AutomationReportAssets.class.getClassLoader().getResourceAsStream(withoutLeadingSlash);
    }
}
