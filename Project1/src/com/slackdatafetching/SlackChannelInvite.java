package com.slackdatafetching;

import com.secrets.Secrets;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsInviteRequest;
import com.slack.api.methods.response.conversations.ConversationsInviteResponse;
import com.slack.api.methods.SlackApiException;
import java.io.IOException;
import java.util.Collections;

public class SlackChannelInvite {

    public static void inviteUserToChannel(String channelId, String userId) {
        String token = Secrets.getSlackBotToken();
        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods(token);
        ConversationsInviteRequest inviteRequest = ConversationsInviteRequest.builder()
                .channel(channelId) 
                .users(Collections.singletonList(userId))
                .build();

        try {
            ConversationsInviteResponse inviteResponse = methods.conversationsInvite(inviteRequest);
            if (inviteResponse.isOk()) {
                System.out.println("Successfully invited the user to the channel.");
            } else {
                System.out.println("Failed to invite the user to the channel. Error: " + inviteResponse.getError());
            }
        } catch (IOException | SlackApiException e) {
            e.printStackTrace();
        }
    }

    //public static void main(String[] args) {
        //String channelId = "CHANNEL_ID"; 
        //String userId = "USER_ID"; 
        
        //inviteUserToChannel(channelId, userId);
    //}
}

