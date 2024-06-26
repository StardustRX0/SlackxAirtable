package com.slackdatafetching;

import com.google.gson.GsonBuilder;
import com.airtableapi.AirTableAPI;
import com.json.JsonUtils;
import com.secrets.Secrets;
import com.google.gson.Gson;
import com.slack.api.Slack;
import com.slack.api.methods.MethodsClient;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.conversations.ConversationsListRequest;
import com.slack.api.methods.request.conversations.ConversationsMembersRequest;
import com.slack.api.methods.request.users.UsersInfoRequest;
import com.slack.api.methods.request.users.UsersListRequest;
import com.slack.api.methods.response.conversations.ConversationsListResponse;
import com.slack.api.methods.response.conversations.ConversationsMembersResponse;
import com.slack.api.methods.response.users.UsersInfoResponse;
import com.slack.api.methods.response.users.UsersListResponse;
import com.slack.api.model.Conversation;
import com.slack.api.model.ConversationType;
import com.slack.api.model.User;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SlackDataFetching {

    private SlackDataFetching() {
        throw new IllegalStateException(" Utility class");
        
    }
    // Slack API credentials
    static String slackToken = Secrets.getSlackBotToken();
    static String userToken = Secrets.getSlackUserToken();

    public static void airtableFetching() throws IOException {
        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods();

        // get list channels from Slack
        List<Conversation> slackChannels = fetchAllChannels(slack, methods);
        JSONArray slackChannelArray = convertToJSONArray(extractChannelData(slackChannels));

        // get list users from Slack
        List<User> slackUsers = fetchUsers(methods);
        assert slackUsers != null;
        JSONArray slackUserArray = convertToJSONArray(extractUserData(slackUsers));

        JSONArray airtableChannelArray = AirTableAPI.listRecords(Secrets.getTableChannelsID());
        JSONArray airtableUserArray = AirTableAPI.listRecords(Secrets.getTableUsersID());

        JSONObject usersInChannel = fetchChannelsWithUsers(slack, slackChannels);

        for (int j = 0; j < slackChannelArray.length(); j++) {
            JSONObject slackChannelObject = slackChannelArray.getJSONObject(j);
            String channelName = slackChannelObject.getString("name");
            JSONArray usersField = usersInChannel.getJSONArray(channelName);
            ArrayList<String> usersIdArray = new ArrayList<>();

            for (int i = 0; i < usersField.length(); i++) {
                String userId = usersField.getString(i);
                String existingRecordId = JsonUtils.findIdInAirtableJsonArray(userId, airtableUserArray);

                if (existingRecordId != null) {
                    usersIdArray.add(existingRecordId);
                }
            }

            slackChannelObject.put("Users", usersIdArray);
            AirTableAPI.createOrUpdateRecord(Secrets.getTableChannelsID(), slackChannelObject, airtableChannelArray);
        }

        for (int j = 0; j < airtableChannelArray.length(); j++) {
            JSONObject airtableChannelObject = airtableChannelArray.getJSONObject(j);
            AirTableAPI.checkDeletedRecord(Secrets.getTableChannelsID(), airtableChannelObject, slackChannelArray);
        }

        for (int i = 0; i < slackUserArray.length(); i++) {
            JSONObject slackUserObject = slackUserArray.getJSONObject(i);
            AirTableAPI.createOrUpdateRecord(Secrets.getTableUsersID(), slackUserObject, airtableUserArray);
        }

        for (int i = 0; i < airtableUserArray.length(); i++) {
            JSONObject airtableUserObject = airtableUserArray.getJSONObject(i);
            AirTableAPI.checkDeletedRecord(Secrets.getTableUsersID(), airtableUserObject, slackUserArray);
        }
    }

    public static void printChannels() {
        Slack slack = Slack.getInstance();
        System.out.println(1);
        MethodsClient methods = slack.methods();
        System.out.println(2);
        List<Conversation> channels = fetchAllChannels(slack, methods);
        System.out.println(3);
        String channelsString = convertToString(extractChannelData(channels));
        JSONArray channelsJSON = new JSONArray(channelsString);
        System.out.format("%-30s %-20s %-20s %-20s %-10s %-10s %-30s %-50s%n", "Name", "ID", "Creator",
                "Create Date", "Privacy", "Status", "Topic", "Description");
        System.out.println(
                "--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");

        // Print the data
        for (int i = 0; i < channelsJSON.length(); i++) {
            JSONObject channel = channelsJSON.getJSONObject(i);
            String name = channel.optString("name", "");
            String id = channel.optString("id", "");
            String topic = channel.optString("topic", "");
            if (topic.length() > 25) {
                topic = topic.substring(0, 25) + "...";
            }
            String description = channel.optString("description", "");
            if (description.length() > 50) {
                description = description.substring(0, 50) + "...";
            }
            String creator = channel.optString("creator", "");
            String createDate = channel.optString("createDate", "");
            String privacy = channel.optString("privacy", "");
            String status = channel.optString("status", "");
            System.out.format("%-30s %-20s %-20s %-20s %-10s %-10s %-30s %-50s%n", name, id, creator, createDate,
                    privacy, status, topic, description);
        }
    }

    public static void printUsers() {
        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods();

        List<User> users = fetchUsers(methods);
        if (users != null) {
            String usersString = convertToString(extractUserData(users));
            JSONArray usersJSON = new JSONArray(usersString);
            System.out.format("%-20s %-20s %-30s %-20s %-20s %-10s %-10s %-20s %-20s%n", "Name", "ID", "Email",
                    "Display Name", "Full Name", "Status", "Role", "User Create Date", "Status Change Date");
            System.out.println(
                    "----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------");
            for (int i = 0; i < usersJSON.length(); i++) {
                JSONObject user = usersJSON.getJSONObject(i);
                String name = user.optString("name", "");
                String id = user.optString("id", "");
                String email = user.optString("email", "");
                String displayName = user.optString("displayName", "");
                String fullName = user.optString("fullName", "");
                String status = user.optString("status", "");
                String role = user.optString("role", "");
                String userCreateDate = user.optString("userCreateDate", "");
                String statusChangeDate = user.optString("statusChangeDate", "");
                System.out.format("%-20s %-20s %-30s %-20s %-20s %-10s %-10s %-20s %-20s%n", name, id, email,
                        displayName, fullName, status, role, userCreateDate, statusChangeDate);
            }
        }
    }

    public static List<Conversation> fetchChannels(MethodsClient methods, String token) {
        if (isNetworkAvailable()) {
            System.out.println("Error: No network connection or the Slack API host is not reachable.");
            return null;
        }
        try {
            ConversationsListRequest request = ConversationsListRequest.builder()
                    .token(token)
                    .types(Arrays.asList(ConversationType.PUBLIC_CHANNEL, ConversationType.PRIVATE_CHANNEL))
                    .build();
            ConversationsListResponse response = methods.conversationsList(request);
            if (response.isOk()) {
                // Filter out and remove duplicate channels based on their IDs
                List<Conversation> channels = response.getChannels();
                if (channels != null) {
                    Set<String> channelIds = new HashSet<>();
                    List<Conversation> uniqueChannels = new ArrayList<>();

                    for (Conversation channel : channels) {
                        if (!channelIds.contains(channel.getId())) {
                            channelIds.add(channel.getId());
                            uniqueChannels.add(channel);
                        }
                    }

                    return uniqueChannels;
                } else {
                    System.out.println("Failed to fetch channels: The 'channels' list is null.");
                }
            } else {
                System.out.println("Failed to fetch channels: " + response.getError());
            }
        } catch (IOException | SlackApiException e) {
            System.out.println("Error occurred while fetching channels: " + e.getMessage());
        }
        return Collections.emptyList();
    }



    public static List<Conversation> fetchAllChannels(Slack slack, MethodsClient methods) {
        List<Conversation> slackChannels = new ArrayList<>();

        // Fetch user's channels using user token
        List<Conversation> userChannels = fetchChannels(methods, userToken);

        // Fetch bot's channels using bot token
        List<Conversation> botChannels = fetchChannels(methods, slackToken);

        // Merge the results and remove duplicates
        Set<String> channelIds = new HashSet<>();
        assert userChannels != null;
        for (Conversation channel : userChannels) {
            if (channelIds.add(channel.getId())) {
                slackChannels.add(channel);
            }
        }
        assert botChannels != null;
        for (Conversation channel : botChannels) {
            if (channelIds.add(channel.getId())) {
                slackChannels.add(channel);
            }
        }

        return slackChannels;
    }


    public static List<User> fetchUsers(MethodsClient methods) {
        if (isNetworkAvailable()) {
            System.out.println("Error: No network connection or the Slack API host is not reachable.");
            return null;
        }
        try {
            UsersListRequest request = UsersListRequest.builder()
                    .token(slackToken)
                    .build();
            UsersListResponse response = methods.usersList(request);
            if (response.isOk()) {
                List<User> users = response.getMembers();
                if (users != null) {
                    // Filter out bot users
                    List<User> filteredUsers = new ArrayList<>();
                    for (User user : users) {
                        if (!user.isBot()) {
                            filteredUsers.add(user);
                        }
                    }
                    return filteredUsers;
                } else {
                    System.out.println("Failed to fetch users: The 'users' list is null.");
                }
            } else {
                System.out.println("Failed to fetch users: " + response.getError());
            }
        } catch (IOException | SlackApiException e) {
            System.out.println("Error occurred while fetching users: " + e.getMessage());
        }
        return Collections.emptyList();
    }


    public static JSONObject fetchChannelsWithUsers(Slack slack, List<Conversation> channels) {
        JSONObject channelUsersObject = new JSONObject();
        try {
            for (Conversation channel : channels) {
                String channelId = channel.getId();
                String channelName = channel.getName();

                // Fetch members using bot token
                List<String> botTokenMemberIds = fetchChannelMembers(slack, slackToken, channelId);
                if (botTokenMemberIds != null) {
                    channelUsersObject.put(channelName, botTokenMemberIds);
                }

                // Fetch members using user token
                List<String> userTokenMemberIds = fetchChannelMembers(slack, userToken, channelId);
                if (userTokenMemberIds != null) {
                    channelUsersObject.put(channelName, userTokenMemberIds);
                }
            }
        } catch (IOException | SlackApiException e) {
            System.out.println("Error occurred while fetching channels with users: " + e.getMessage());
        }
        return channelUsersObject;
    }



    private static List<String> fetchChannelMembers(Slack slack, String token, String channelId) throws IOException, SlackApiException {
        ConversationsMembersRequest request = ConversationsMembersRequest.builder()
                .token(token)
                .channel(channelId)
                .build();

        ConversationsMembersResponse response = slack.methods().conversationsMembers(request);

        if (response.isOk()) {
            return response.getMembers();
        } else {
            return Collections.emptyList(); // Return an empty list in case of failure
        }
    }


    public static List<ChannelData> extractChannelData(List<Conversation> channels) {
        List<ChannelData> channelDataList = new ArrayList<>();
        for (Conversation channel : channels) {
            ChannelData channelData = new ChannelData();
            channelData.setName(channel.getName());
            channelData.setId(channel.getId());
            channelData.setTopic(channel.getTopic().getValue());
            channelData.setDescription(channel.getPurpose().getValue());
            channelData.setCreator(channel.getCreator());
            channelData.setCreateDate(formatDate(channel.getCreated()));
            channelData.setPrivacy(channel.isPrivate() ? "Private" : "Public");
            channelData.setStatus(channel.isArchived() ? "Archived" : "Active");
            channelDataList.add(channelData);
        }
        return channelDataList;
    }

    public static List<UserData> extractUserData(List<User> users) {
        List<UserData> userDataList = new ArrayList<>();
        for (User user : users) {
            if (user.isBot() || "USLACKBOT".equals(user.getId())) {
                continue;
            }
            UserData userData = new UserData();
            userData.setName(user.getName());
            userData.setId(user.getId());
            userData.setEmail(user.getProfile().getEmail());
            userData.setDisplayName(user.getProfile().getDisplayName());
            userData.setFullName(user.getProfile().getRealName());
            userData.setStatus(user.getProfile().getStatusText());
            userData.setRole(user.isOwner() ? "Owner" : "Member");
            userData.setUserCreateDate(formatDate(user.getUpdated()));
            userData.setStatusChangeDate(formatDate(user.getProfile().getStatusExpiration()));
            userDataList.add(userData);
        }
        return userDataList;
    }

    public static void printChannelsWithUsers() {
        Slack slack = Slack.getInstance();
        MethodsClient methods = slack.methods();
        List<Conversation> channels = fetchAllChannels(slack, methods);

        JSONObject channelUsersObject = fetchChannelsWithUsers(slack, channels);
        System.out.println("Channels:");

        for (Conversation channel : channels) {
            String channelId = channel.getId();
            String channelName = channel.getName();

            System.out.println("Channel: " + channelName + " (ID: " + channelId + ")");

            if (channelUsersObject.has(channelName)) {
                JSONArray memberIds = channelUsersObject.getJSONArray(channelName);
                List<User> members = new ArrayList<>();

                for (int i = 0; i < memberIds.length(); i++) {
                    String memberId = memberIds.getString(i);
                    UsersInfoResponse userInfoResponse;
                    try {
                        userInfoResponse = slack.methods(slackToken)
                                .usersInfo(UsersInfoRequest.builder().user(memberId).build());
                        if (userInfoResponse.isOk()) {
                            User user = userInfoResponse.getUser();
                            members.add(user);
                        }
                    } catch (IOException | SlackApiException e) {
                        System.out.println("Error occurred while fetching user info: " + e.getMessage());
                    }
                }

                System.out.println("Users in Channel:");

                for (User user : members) {
                    System.out.println("  - " + user.getName() + " (ID: " + user.getId() + ")");
                }
            }

            System.out.println();
        }
    }

    private static String formatDate(Number timestamp) {
        if (timestamp != null && timestamp.longValue() > 0) {
            long timestampValue = timestamp.longValue();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            return sdf.format(timestampValue * 1000);
        }
        return "N/A";
    }

    private static JSONArray convertToJSONArray(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(object);
        return new JSONArray(json);
    }

    private static String convertToString(Object object) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(object);
    }

    private static boolean isNetworkAvailable() {
        try {
            InetAddress.getByName("slack.com");
            return false;
        } catch (UnknownHostException e) {
            return true;
        }
    }

}

