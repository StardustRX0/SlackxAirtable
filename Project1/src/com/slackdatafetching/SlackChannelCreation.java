package com.slackdatafetching;


import com.secrets.Secrets;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.request.conversations.ConversationsCreateRequest;
import com.slack.api.methods.request.conversations.ConversationsJoinRequest;
import com.slack.api.methods.response.conversations.ConversationsCreateResponse;
import com.slack.api.methods.response.conversations.ConversationsJoinResponse;

public class SlackChannelCreation {

    public static void createAndJoinChannel(String channelName) {

        String token = Secrets.getSlackBotToken(); 

        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods(token);

        ConversationsCreateRequest createRequest = ConversationsCreateRequest.builder()
                .name(channelName) 
                .isPrivate(false) 
                .build();

        try {
            ConversationsCreateResponse createResponse = methods.conversationsCreate(createRequest);
            if (createResponse.isOk()) {
                String channelId = createResponse.getChannel().getId();
                System.out.println("Channel created successfully. Channel ID: " + channelId);

                joinChannel(methods, channelId);
            } else {
                System.out.println("Failed to create channel. Error: " + createResponse.getError());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void joinChannel(MethodsClient methods, String channelId) {

        ConversationsJoinRequest joinRequest = ConversationsJoinRequest.builder()
                .channel(channelId) 
                .build();

        try {

            ConversationsJoinResponse joinResponse = methods.conversationsJoin(joinRequest);
            if (joinResponse.isOk()) {
                System.out.println("Successfully joined the channel.");
            } else {
                System.out.println("Failed to join the channel. Error: " + joinResponse.getError());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String channelName = "new_channel";
        createAndJoinChannel(channelName);
    }
}
