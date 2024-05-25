package com.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.airtableapi.AirTableAPI;
import com.airtableapi.AirTableAPI;
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
        String menu = "\nPlease select an option:\n\n" + "1. Show Slack's channels\n"
                + "2. Show Slack user's information\n" + "3. Create a channels\n"
                + "4. Invite user to channel\n" + "5. Fetching slack to airtable\n"
                + "0. Exit\n\n" + "Enter your choice (0-5): ";

        System.out.print(menu);

        try {
            int option = sc.nextInt();
            switch (option) {
                case 0:
                    System.out.println("Program ended!");
                    break;
                case 1:
                    showChannels();
                    break;
                case 2:
                    showUsers();
                    break;
                case 3:
                    //createChannel();
                    break;
                case 4:
                    //inviteUser();
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

    public static void showMenuFetching() throws IOException, SlackApiException{
        String menu = "\nPlease select an option:\n\n" + "1. Fetch data from Slack to AirTable\n" + "2. Schedule fetching task";
        System.out.println(menu);
        int option = sc.nextInt();

        switch (option) {
            case 0:
                showMenu();
                break;
            case 1:
                LocalDateTime submittedTime = LocalDateTime.now();
                //autoFetching(submittedTime, "Manual sync");
                System.out.println("Syncing data...");
                System.out.println("Press Enter key to get back...");
                System.in.read();
                showMenu();

                break;
            case 2:
                //taskScheduling("Schedule sync");
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                LocalDateTime startedTime = LocalDateTime.now();
                String status = "SUCCESS";
                try {
                    SlackDataFetching.airtableFetching();
                } catch (Exception e) {
                    status = "FAILED";
                    Logger logger = LoggerFactory.getLogger(SlackDataFetching.class);
                    logger.error("Error occurred during data fetching: " + e.getMessage());
                }
                LocalDateTime finishedTime = LocalDateTime.now();
                AirTableAPI.createLogs(submittedTime, startedTime, finishedTime, status, task);
            }
        });
        thread.setName("DataFetchingThread-" + task);
        thread.start();
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
