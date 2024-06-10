package com.main;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.airtableapi.AirTableAPI;
import com.slackdatafetching.SlackChannelCreation;
import com.slackdatafetching.SlackChannelInvite;
import com.slackdatafetching.SlackDataFetching;
import com.slack.api.methods.SlackApiException;

public class Main {

    private static Scanner sc = new Scanner(System.in);
    private static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    public static void main(String[] args) throws Exception {
        showlogo();
        showMenu();
        sc.close();
    }

    public static void showlogo() {

        String title = "WELCOME TO SLACK MANAGEMENT PROGRAM!";
        int width = 60;
        String line = "-".repeat(width);

        System.out.println(line);
        System.out.println(centerString(title, width));
        System.out.println(line);
    }

    public static void showMenu() throws IOException, SlackApiException {
        String menu = """

                Please select an option:

                1. Show Slack's channels
                2. Show Slack user's information
                3. Create a channels
                4. Invite user to channel
                5. Fetching slack to airtable
                0. Exit

                Enter your choice (0-5):\s""";

        System.out.print(menu);

        try {
            int option = sc.nextInt();
            switch (option) {
                case 0:
                    System.out.println("Program ended!");
                    System.exit(0);
                case 1:
                    showChannels();
                    break;
                case 2:
                    showUsers();
                    break;
                case 3:
                    createChannel();
                    break;
                case 4:
                    inviteUser();
                    break;
                case 5:
                    showMenuFetching();
                    break;
                default:
                    System.out.println("Invalid input! Please enter a valid option (0-5).");
                    showMenu();
                    break;
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input! Please enter a valid option (0-5).");
            sc.next();
            showMenu();
        }
    }

    private static void inviteUser() throws IOException, SlackApiException {
    	 System.out.print("Enter the channelId: ");
         String channelId = sc.next();
         System.out.print("Enter the userId: ");
         String userId = sc.next();
         
         try {
             SlackChannelInvite.inviteUserToChannel(channelId, userId);
             
             System.out.println("Channel invited successfully new user!");
         } catch (Exception e) {
             System.out.println("Failed to invite new user! with error : " + e.getMessage());
         }
         
         showMenu();
		
	}

	private static void createChannel() throws IOException, SlackApiException {
		System.out.print("Enter the name for the new channel: ");
        String channelName = sc.next();
        
        try {
            SlackChannelCreation.createAndJoinChannel(channelName);
            
            System.out.println("Channel created successfully and added to the current channel!");
        } catch (Exception e) {
            System.out.println("Failed to create channel and add to the current channel. Error: " + e.getMessage());
        }
        
        showMenu();
    }

		

	public static void showMenuFetching() throws IOException, SlackApiException{
        String menu = """

                Please select an option:

                1. Fetch data from Slack to AirTable
                2. Schedule fetching task""";
        System.out.println(menu);
        int option = sc.nextInt();

        switch (option) {
            case 0:
                showMenu();
                break;
            case 1:
                LocalDateTime submittedTime = LocalDateTime.now();
                autoFetching(submittedTime, "Manual sync");
                System.out.println("Syncing data...");
                System.out.println("Press Enter key to get back...");
                System.in.read();
                showMenu();

                break;
            case 2:
                taskScheduling("Schedule sync");
                System.out.println("Press Enter key to get back...");
                System.in.read();
                showMenu();
                break;
            default:
                System.out.println("Invalid input!");
                showMenuFetching();
                break;
        }
    }


    public static void autoFetching(final LocalDateTime submittedTime, final String task) {
        Thread thread = new Thread(() ->
        {
            LocalDateTime startedTime = LocalDateTime.now();
            String status = "SUCCESS";
            try {
                SlackDataFetching.airtableFetching();
            } catch (Exception e) {
                status = "FAILED";
                Logger logger = LoggerFactory.getLogger(SlackDataFetching.class);
                logger.error("Error occurred during data fetching: {}", e.getMessage());
            }
            LocalDateTime finishedTime = LocalDateTime.now();
            AirTableAPI.createLogs(submittedTime, startedTime, finishedTime, status, task);
        });
        thread.setName("DataFetchingThread-" + task);
        thread.start();
    }

    public static void taskScheduling(final String task) {
        Scanner scanner = new Scanner(System.in);

        int hour;
        int minute;

        while (true) {
            System.out.print("Enter hour (0-23): ");
            if (scanner.hasNextInt()) {
                hour = scanner.nextInt();
                if (hour >= 0 && hour <= 23) {
                    break;
                } else {
                    System.out.println("Invalid value for hour! Please enter a value between 0 and 23.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid integer for the hour.");
                scanner.next(); // Consume the incorrect input
            }
        }

        while (true) {
            System.out.print("Enter minute (0-59): ");
            if (scanner.hasNextInt()) {
                minute = scanner.nextInt();
                if (minute >= 0 && minute <= 59) {
                    break;
                } else {
                    System.out.println("Invalid value for minute! Please enter a value between 0 and 59.");
                }
            } else {
                System.out.println("Invalid input! Please enter a valid integer for the minute.");
                scanner.next(); // Consume the incorrect input
            }
        }

        final LocalDateTime submittedScheduleTime = LocalDateTime.now();

        System.out.printf("Data will be syncing at %d:%02d\n", hour, minute);


        scheduler.scheduleAtFixedRate(() -> autoFetching(submittedScheduleTime, task), getDelay(hour, minute), 24L * 60 * 60 * 1000, TimeUnit.MILLISECONDS);
    }

    private static long getDelay(int hour, int minute) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = LocalDateTime.of(now.getYear(), now.getMonth(), now.getDayOfMonth(), hour, minute);

        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }

        return ChronoUnit.MILLIS.between(now, nextRun);
    }

    public static void showUsers() throws IOException, SlackApiException {
        SlackDataFetching.printUsers();
        System.out.println("Press Enter key to get back...");
        System.in.read();
        showMenu();
    }

    public static void showChannels() throws IOException, SlackApiException  {
        SlackDataFetching.printChannels();
        System.out.println("Press Enter key to get back...");
        System.in.read();
        showMenu();
    }

    public static String centerString(String text, int width) {
        if (text.length() > width) {
            return text.substring(0, width);
        } else {
            int padding = width - text.length();
            int leftPadding = padding / 2;
            int rightPadding = padding - leftPadding;
            return " ".repeat(leftPadding) + text + " ".repeat(rightPadding);
        }
    }
    

}
