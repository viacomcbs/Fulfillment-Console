package com.paramount.test.ff.common.util;

import com.synergy.core.driver.ServerPostHandler;
import com.synergy.core.exceptions.ServerFailureException;
import com.synergy.core.httpexecutor.HttpClient;
import org.json.simple.JSONObject;

@SuppressWarnings("unchecked")
public class SlackMessenger {

	private final ServerPostHandler serverPostHandler ;
	private final HttpClient httpClient;

	private final String webHookURL;

	/**
	 * Initiates the Slack Messenger for sending a Slack message to a channel
	 * from a test execution.
	 *
	 * @param serverURL  - String - The url of the listening synergy server session.
	 * @param webhookURL - String - The webhook url of an integrated Slack "Incoming WebHooks" app. Contact your team lead to obtain this.
	 */
	public SlackMessenger(String serverURL, String webhookURL) {
		httpClient = new HttpClient(serverURL);
		serverPostHandler = new ServerPostHandler(httpClient);

		webHookURL = webhookURL;
	}

	/**
	 * Sends mrkdwn formatted message to the channel with associated icon.
	 *
	 * @param channel    - String - The channel name where you want to post this message to
	 * @param messageText    - String - mrkdwn formatted message
	 * @param icon - String The url to an associated image or the slack emoji you'd like to
	 *                 showcase in the message
	 *                 NOTE: We provide a few sample images
	 *                 that may be helpful for expressing test run result status:
	 *                 https://s3.amazonaws.com/mqeappprodpackages/success.png
	 *                 https://s3.amazonaws.com/mqeappprodpackages/failure.png
	 *                 https://s3.amazonaws.com/mqeappprodpackages/caution.png
	 *                 https://s3.amazonaws.com/mqeappprodpackages/info.png
	 *
	 * @throws ServerFailureException - If the Slack message cannot be sent for any
	 *                                reason.
	 */
	public void sendMessage(String channel, String messageText, String icon) {
		send(channel, messageText, icon);
	}

	/**
	 * Sends mrkdwn formatted message to the channel.
	 *
	 * @param channel - String - The channel name where you want to post this message to
	 * @param messageText  - String - mrkdwn formatted message
	 * 
	 * @throws ServerFailureException - If the Slack message cannot be sent for any
	 *                                reason.
	 */
	public void sendMessage(String channel, String messageText) {
		send(channel, messageText, null);
	}

	private void send(String channel, String messageText, String icon) {
		if (channel == null || channel.isEmpty()) {
			throw new ServerFailureException("Channel name is required.");
		}

		if (messageText == null || messageText.isEmpty()) {
			throw new ServerFailureException("The message text is required.");
		}

		JSONObject slackObj = new JSONObject();
		slackObj.put("action", "send_message");
		slackObj.put("webhook_url", String.valueOf(webHookURL));
		slackObj.put("channel", channel);
		if (icon != null && !icon.isEmpty()) {
			slackObj.put("icon", icon);
		}
		slackObj.put("message_text", messageText);
		serverPostHandler.postToServerWithHandling("slackmessenger", slackObj, ServerFailureException.class);
	}

}
