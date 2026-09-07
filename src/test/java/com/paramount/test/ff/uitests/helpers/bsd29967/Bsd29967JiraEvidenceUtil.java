package com.paramount.test.ff.uitests.helpers.bsd29967;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/** Posts BSD-29967 per-workflow DSID table to Jira when the suite passes. */
public final class Bsd29967JiraEvidenceUtil {

    private static final String JIRA_BASE_URL = "https://paramount.atlassian.net";
    private static final String ENV_EMAIL = "JIRA_API_EMAIL";
    private static final String ENV_TOKEN = "JIRA_API_TOKEN";

    private Bsd29967JiraEvidenceUtil() {
    }

    /**
     * Publishes accumulated PASS workflow rows (with DSIDs) to BSD-29967 without a live suite run.
     * Run via: mvn -q exec:java -Dexec.classpathScope=test
     *   -Dexec.mainClass=com.paramount.test.ff.uitests.helpers.bsd29967.Bsd29967JiraManualPost
     *   -Dsystem.test.jiraapiemail=... -Dsystem.test.jiraapitoken=...
     */
    public static void publishAccumulatedEvidenceNow() {
        List<Bsd29967EnvironmentResult> accumulated = Bsd29967AccumulatedResultsStore.loadAllPassRows();
        if (accumulated.isEmpty()) {
            Logger.logReportMessage("BSD-29967 — nothing to publish (accumulated store is empty)");
            return;
        }
        String issueKey = resolveIssueKey();
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(new Date());
        try {
            Path evidenceDir = Paths.get("test-output", "jira-evidence");
            Files.createDirectories(evidenceDir);
            Path htmlFile = evidenceDir.resolve(Bsd29967EmailReport.JIRA_KEY
                    + "_WorkflowResults_Combined_" + stamp + ".html");
            String tableHtml = Bsd29967EmailReport.buildWorkflowResultsTableHtml(accumulated);
            String html = wrapEvidenceDocument(tableHtml);
            Files.write(htmlFile, html.getBytes(StandardCharsets.UTF_8));
            Logger.logReportMessage("BSD-29967 — combined table saved: " + htmlFile.toAbsolutePath()
                    + " (" + accumulated.size() + " workflow(s))");

            JiraCredentials credentials = resolveCredentials();
            if (credentials == null) {
                Logger.logReportMessage("BSD-29967 — set " + ENV_EMAIL + " and " + ENV_TOKEN
                        + " (or -Dsystem.test.jiraapiemail / jiraapitoken) to post to Jira");
                return;
            }
            addComment(issueKey, buildCommentBody(stamp, accumulated.size(), accumulated.size()), credentials);
            attachFile(issueKey, htmlFile, credentials);
            Logger.logReportMessage("BSD-29967 — published to " + issueKey + " (" + accumulated.size()
                    + " workflows)");
        } catch (Exception e) {
            Logger.logReportMessage("BSD-29967 — manual Jira publish failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void attachEvidenceOnPassIfConfigured() {
        finalizeEvidenceAfterSuite();
    }

    /**
     * Merges PASS workflows with populated DSIDs into the accumulated store, writes combined HTML locally,
     * and posts Jira comment + attachment when API credentials are configured.
     */
    public static void finalizeEvidenceAfterSuite() {
        if (!Bsd29967EmailReport.isEnabled()) {
            return;
        }
        if (Bsd29967EmailReport.getFailedCount() > 0) {
            Bsd29967EmailReport.setJiraEvidenceStatus("skipped — test failures present");
            return;
        }
        if (Bsd29967EmailReport.getFailedEnvironmentCount() > 0) {
            Bsd29967EmailReport.setJiraEvidenceStatus("skipped — workflow validation failures present");
            return;
        }
        if (Bsd29967EmailReport.getPassedEnvironmentCount() == 0) {
            Bsd29967EmailReport.setJiraEvidenceStatus("skipped — no PASS workflows with DSIDs");
            return;
        }

        String issueKey = resolveIssueKey();
        String stamp = new SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(new Date());
        try {
            Path evidenceDir = Paths.get("test-output", "jira-evidence");
            Files.createDirectories(evidenceDir);
            Path htmlFile = evidenceDir.resolve(Bsd29967EmailReport.JIRA_KEY
                    + "_WorkflowResults_Combined_" + stamp + ".html");

            List<Bsd29967EnvironmentResult> accumulated =
                    Bsd29967AccumulatedResultsStore.mergeCurrentRunPassRows(
                            Bsd29967EmailReport.getCurrentRunPassRowsWithDsids());
            int currentRunPass = Bsd29967EmailReport.getPassedEnvironmentCount();
            String tableHtml = Bsd29967EmailReport.buildWorkflowResultsTableHtml(accumulated);
            String html = wrapEvidenceDocument(tableHtml);
            Files.write(htmlFile, html.getBytes(StandardCharsets.UTF_8));
            Logger.logReportMessage("BSD-29967 — combined workflow table saved: "
                    + htmlFile.toAbsolutePath() + " (" + accumulated.size() + " workflow(s))");

            if (!Bsd29967EmailReport.isAttachJiraOnPassEnabled()) {
                Bsd29967EmailReport.setJiraEvidenceStatus("local file only — AttachJiraEvidenceOnPass=false");
                return;
            }

            JiraCredentials credentials = resolveCredentials();
            if (credentials == null) {
                Bsd29967EmailReport.setJiraEvidenceStatus(
                        "local files only — set " + ENV_EMAIL + " and " + ENV_TOKEN + " to auto-attach");
                return;
            }

            addComment(issueKey, buildCommentBody(stamp, currentRunPass, accumulated.size()), credentials);
            attachFile(issueKey, htmlFile, credentials);
            Bsd29967EmailReport.setJiraEvidenceStatus("attached to " + issueKey);
            Logger.logReportMessage("BSD-29967 — combined workflow table attached to " + issueKey
                    + " (" + accumulated.size() + " workflows)");
        } catch (Exception e) {
            Bsd29967EmailReport.setJiraEvidenceStatus("attach failed — " + e.getMessage());
            Logger.logReportMessage("BSD-29967 — Jira evidence finalize failed: " + e.getMessage());
        }
    }

    private static String resolveIssueKey() {
        String fromConfig = Config.getString("JiraTicketID");
        if (fromConfig != null && !fromConfig.trim().isEmpty()) {
            return fromConfig.trim();
        }
        return Bsd29967EmailReport.JIRA_KEY;
    }

    private static String wrapEvidenceDocument(String tableHtml) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"/>"
                + "<title>" + Bsd29967EmailReport.JIRA_KEY + " — per-workflow DSID validation</title>"
                + "</head><body>" + tableHtml + "</body></html>";
    }

    private static String buildCommentBody(String stamp, int currentRunPass, int totalAccumulated) {
        String env = Config.getString("TestEnvironment");
        String testEnv = env == null || env.trim().isEmpty() ? "DEV" : env.trim().toUpperCase(Locale.ROOT);
        return "BSD-29967 DEV automation — per-workflow DSID validation (" + stamp + ")\n\n"
                + "Test Environment: " + testEnv + "\n"
                + "This run: " + currentRunPass + " workflow(s) PASS with populated DSIDs.\n"
                + "Combined table attached: " + totalAccumulated + " workflow(s) PASS across runs.\n\n"
                + "The Details tab Identifiers section displays only DSIDs from the most recent job, as expected.\n"
                + "Superseded job DSIDs no longer appear alongside current DSIDs, as expected.";
    }

    private static JiraCredentials resolveCredentials() {
        String email = firstNonEmpty(
                System.getenv(ENV_EMAIL),
                System.getProperty("system.test.jiraapiemail"),
                Config.getString("JiraApiEmail"));
        String token = firstNonEmpty(
                System.getenv(ENV_TOKEN),
                System.getProperty("system.test.jiraapitoken"),
                Config.getString("JiraApiToken"));
        if (email == null || token == null) {
            return null;
        }
        return new JiraCredentials(email.trim(), token.trim());
    }

    private static String firstNonEmpty(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value.trim();
            }
        }
        return null;
    }

    private static void addComment(String issueKey, String commentBody, JiraCredentials credentials)
            throws IOException {
        JSONObject paragraph = new JSONObject();
        paragraph.put("type", "paragraph");
        org.json.simple.JSONArray content = new org.json.simple.JSONArray();
        JSONObject text = new JSONObject();
        text.put("type", "text");
        text.put("text", commentBody);
        content.add(text);
        paragraph.put("content", content);

        JSONObject body = new JSONObject();
        body.put("type", "doc");
        body.put("version", 1);
        org.json.simple.JSONArray bodyContent = new org.json.simple.JSONArray();
        bodyContent.add(paragraph);
        body.put("content", bodyContent);

        JSONObject payload = new JSONObject();
        payload.put("body", body);

        HttpPost post = new HttpPost(JIRA_BASE_URL + "/rest/api/3/issue/" + issueKey + "/comment");
        post.setHeader(HttpHeaders.AUTHORIZATION, credentials.basicAuthHeader());
        post.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        post.setEntity(new StringEntity(payload.toJSONString(), ContentType.APPLICATION_JSON));
        execute(post, credentials);
    }

    private static void attachFile(String issueKey, Path file, JiraCredentials credentials) throws IOException {
        HttpPost post = new HttpPost(JIRA_BASE_URL + "/rest/api/3/issue/" + issueKey + "/attachments");
        post.setHeader(HttpHeaders.AUTHORIZATION, credentials.basicAuthHeader());
        post.setHeader("X-Atlassian-Token", "no-check");
        post.setEntity(MultipartEntityBuilder.create()
                .addBinaryBody("file", file.toFile(), ContentType.APPLICATION_OCTET_STREAM, file.getFileName().toString())
                .build());
        execute(post, credentials);
    }

    private static final int JIRA_CONNECT_TIMEOUT_MS = 15_000;
    private static final int JIRA_SOCKET_TIMEOUT_MS = 60_000;

    private static void execute(HttpPost post, JiraCredentials credentials) throws IOException {
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(credentials.email, credentials.token));
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(JIRA_CONNECT_TIMEOUT_MS)
                .setConnectionRequestTimeout(JIRA_CONNECT_TIMEOUT_MS)
                .setSocketTimeout(JIRA_SOCKET_TIMEOUT_MS)
                .build();
        try (CloseableHttpClient client = HttpClients.custom()
                .setDefaultCredentialsProvider(provider)
                .setDefaultRequestConfig(requestConfig)
                .build()) {
            HttpResponse response = client.execute(post);
            int status = response.getStatusLine().getStatusCode();
            String body = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
            if (status < 200 || status >= 300) {
                throw new IOException("Jira API " + status + ": " + body);
            }
        }
    }

    private static final class JiraCredentials {
        private final String email;
        private final String token;

        private JiraCredentials(String email, String token) {
            this.email = email;
            this.token = token;
        }

        private String basicAuthHeader() {
            String raw = email + ":" + token;
            return "Basic " + Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        }
    }
}
