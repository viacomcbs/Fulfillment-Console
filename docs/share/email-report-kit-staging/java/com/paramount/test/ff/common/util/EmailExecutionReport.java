package com.paramount.test.ff.common.util;


import java.io.File;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class EmailExecutionReport {

	protected String username = !Config.getString("senderAddress").equals("") ? Config.getString("senderAddress") : System.getProperty("senderAddress");
	protected String password = !Config.getString("senderPwd").equals("") ? Config.getString("senderPwd") : System.getProperty("senderPwd");
	protected String recipientAddress = !Config.getString("recipientAddress").equals("") ? Config.getString("recipientAddress") : System.getProperty("recipientAddress");
	protected String applicationName = !Config.getString("Application").equals("") ? Config.getString("Application") : System.getProperty("Application");
	String workingDir = System.getProperty("user.dir");
	Session session;

	public void sendReportViaEmail() {
		
		if (Config.getBoolean("sendExecutionReport") || Boolean.getBoolean("sendExecutionReport")) {
			if (checkRunParams()) {

				Properties props = new Properties();
				props.put("mail.smtp.auth", !Config.getString("authenticationReqd").equals("") ? Config.getString("authenticationReqd") : System.getProperty("authenticationReqd"));
				props.put("mail.smtp.host", "vishnupriya.Arumugam@paramount.com");
				props.put("mail.smtp.port", "25");

				if (Config.getBoolean("authenticationReqd") || Boolean.getBoolean("authenticationReqd")) {
					session = Session.getInstance(props, new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(username, password);
						}
					});
				} else {
					session = Session.getInstance(props, null);
				}

				try {

					MimeMessage message = new MimeMessage(session);
					message.setFrom(new InternetAddress(username));
					message.setRecipients(MimeMessage.RecipientType.TO, InternetAddress.parse(recipientAddress));
					message.setSubject("Test Execution Results for " + applicationName + " application");

					// Create the message part
					BodyPart messageBodyPart = new MimeBodyPart();

					// Set actual message
					messageBodyPart.setText("Hi, \n" + "PFA test execution result for " + applicationName
							+ " application." + "\n\n"
							+ "NOTE: This is a system generated email. Kindly do not respond to this. For any queries on the execution, contact respective Team Members"
							+ "\n\n" + "Thanks & Regards,\n" + "Viacom Automation Team");

					// Create a multipart message
					Multipart multipart = new MimeMultipart();

					// Set text message part
					multipart.addBodyPart(messageBodyPart);

					// Add attachment
					messageBodyPart = new MimeBodyPart();
					String filename = workingDir + File.separator + "test-output" + File.separator + "emailable-report.html";
					DataSource source = new FileDataSource(filename);
					messageBodyPart.setDataHandler(new DataHandler(source));
					messageBodyPart.setFileName(applicationName + "_Execution_Report.html");
					multipart.addBodyPart(messageBodyPart);
					message.setContent(multipart);

					Transport.send(message);

					System.out.println("Email sent successfully!");

				} catch (MessagingException e) {

					System.out.println("Exception occured while sending report.");
					throw new RuntimeException(e);
				}

			} else {
				System.out.println("Email not sent because all mandatory parameters not provided");
			}
		} else {
			System.out.println("Email not sent because flag is set to false");
		}
	}
	
	
	public Boolean checkRunParams() {
		Boolean areAllParamsPresent = true;
		
		if (username == null || !username.contains("paramount")) {
			areAllParamsPresent = false;
			System.out.println("Aborting sending email because either username is not provided or it is invalid.");
			System.out.println("Supplied data: "+username);
		}
			
		
		if (password == null && (Config.getBoolean("authenticationReqd") || Boolean.getBoolean("authenticationReqd"))) {
			areAllParamsPresent = false;
			System.out.println("Aborting sending email because password is not provided.");
			System.out.println("Supplied data: "+password);
		}
		
		if (recipientAddress == null) {
			areAllParamsPresent = false;
			System.out.println("Aborting sending email because recepient address is not provided.");
			System.out.println("Supplied data: "+recipientAddress);
		}
		
		if (applicationName == null) {
			areAllParamsPresent = false;
			System.out.println("Aborting sending email because application name is not provided.");
			System.out.println("Supplied data: "+applicationName);
		}
		
		return areAllParamsPresent;
	}

}
