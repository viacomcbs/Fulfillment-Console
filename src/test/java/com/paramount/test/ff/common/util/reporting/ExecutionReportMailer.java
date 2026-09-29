package com.paramount.test.ff.common.util.reporting;

import java.io.File;
import java.nio.file.Path;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import com.paramount.test.ff.common.util.Config;
import com.paramount.test.ff.common.util.Logger;
import com.paramount.test.ff.common.util.props.IProps.ConfigProps;

public final class ExecutionReportMailer {

    private ExecutionReportMailer() {
    }

    public static boolean isSmtpConfigured() {
        String host = Config.getString("EmailRelayHost");
        return host != null && !host.trim().isEmpty();
    }

    public static boolean sendHtmlEmail(String recipients, String subject, String htmlBody) {
        return sendHtmlEmail(recipients, subject, htmlBody, null);
    }

    public static boolean sendHtmlEmailWithPdf(String recipients, String subject, String htmlBody, File pdfAttachment) {
        return sendHtmlEmail(recipients, subject, htmlBody, pdfAttachment, false);
    }

    public static boolean sendHtmlEmailWithInlineLogo(String recipients, String subject, String htmlBody) {
        return sendHtmlEmail(recipients, subject, htmlBody, null, true);
    }

    private static boolean sendHtmlEmail(String recipients, String subject, String htmlBody, File pdfAttachment) {
        return sendHtmlEmail(recipients, subject, htmlBody, pdfAttachment, false);
    }

    private static boolean sendHtmlEmail(String recipients, String subject, String htmlBody, File pdfAttachment,
            boolean inlineLogo) {
        if (recipients == null || recipients.trim().isEmpty()) {
            Logger.logMessage("Email not sent: recipient address is empty.");
            return false;
        }
        if (!isSmtpConfigured()) {
            return false;
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", Config.getString("EmailRelayHost"));
            props.put("mail.smtp.port", Config.getString("EmailRelayPort"));
            props.put("mail.smtp.auth", Config.getString("authenticationReqd"));

            final String sender = resolveSenderAddress();
            final String password = Config.getString("senderPwd");
            boolean authRequired = Config.getBoolean("authenticationReqd");

            Session session;
            if (authRequired) {
                session = Session.getInstance(props, new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(sender, password);
                    }
                });
            } else {
                session = Session.getInstance(props, null);
            }

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            InternetAddress[] toAddresses = InternetAddress.parse(normalizeRecipients(recipients), false);
            message.setRecipients(Message.RecipientType.TO, toAddresses);
            message.setSubject(subject, "UTF-8");

            String resolvedHtml = htmlBody;
            Multipart multipart = new MimeMultipart("related");

            if (inlineLogo && AutomationReportAssets.hasParamountLogo()) {
                Path logoPath = AutomationReportAssets.writeParamountLogoTempFile();
                if (logoPath != null) {
                    MimeBodyPart logoPart = new MimeBodyPart();
                    logoPart.setDataHandler(new DataHandler(new FileDataSource(logoPath.toFile())));
                    logoPart.setHeader("Content-ID", "<" + AutomationReportAssets.PARAMOUNT_LOGO_CID + ">");
                    logoPart.setDisposition(MimeBodyPart.INLINE);
                    logoPart.setFileName("paramount-logo.png");
                    multipart.addBodyPart(logoPart);
                    resolvedHtml = htmlBody.replace(resolveHostedOrDataLogoSrc(htmlBody),
                            "cid:" + AutomationReportAssets.PARAMOUNT_LOGO_CID);
                }
            }

            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(resolvedHtml, "text/html; charset=UTF-8");
            multipart.addBodyPart(htmlPart);

            if (pdfAttachment != null && pdfAttachment.exists()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                FileDataSource source = new FileDataSource(pdfAttachment);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(pdfAttachment.getName());
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);
            Transport.send(message);

            if (pdfAttachment != null && pdfAttachment.exists()) {
                Logger.logMessage("Execution report email sent with PDF attachment to " + recipients);
            } else {
                Logger.logMessage("Execution report email sent to " + recipients);
            }
            return true;
        } catch (Exception e) {
            if (pdfAttachment != null && pdfAttachment.exists()) {
                Logger.logConsoleMessage("Failed to send email with PDF attachment.");
            } else {
                Logger.logConsoleMessage("Failed to send execution report email via SMTP.");
            }
            e.printStackTrace();
            return false;
        }
    }

    private static String normalizeRecipients(String recipients) {
        StringBuilder normalized = new StringBuilder();
        for (String part : recipients.split("[,;]")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                if (normalized.length() > 0) {
                    normalized.append(',');
                }
                normalized.append(trimmed);
            }
        }
        return normalized.toString();
    }

    private static String resolveHostedOrDataLogoSrc(String htmlBody) {
        if (htmlBody == null || htmlBody.isEmpty()) {
            return "";
        }
        int imgIdx = htmlBody.indexOf("<img");
        if (imgIdx < 0) {
            return "";
        }
        int srcIdx = htmlBody.indexOf("src='", imgIdx);
        if (srcIdx < 0) {
            srcIdx = htmlBody.indexOf("src=\"", imgIdx);
            if (srcIdx < 0) {
                return "";
            }
            int start = srcIdx + 5;
            int end = htmlBody.indexOf('"', start);
            return end > start ? htmlBody.substring(start, end) : "";
        }
        int start = srcIdx + 5;
        int end = htmlBody.indexOf('\'', start);
        return end > start ? htmlBody.substring(start, end) : "";
    }

    private static String resolveSenderAddress() {
        String sender = ConfigProps.EMAIL_SENDER_ADDRESS;
        if (sender == null || sender.trim().isEmpty()) {
            sender = Config.getString("senderAddress");
        }
        return sender;
    }
}
