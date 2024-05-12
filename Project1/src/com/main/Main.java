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
        /*String logo = "\n  ___  ___  ___  ___  ___.---------------.\n"
                + ".'\\__\\'\\__\\'\\__\\'\\__\\'\\__,`   .  ____ ___ \\\n"
                + "|\\/ __\\/ __\\/ __\\/ __\\/ _:\\   |`.  \\  \\___ \\\n"
                + " \\\\'\\__\\'\\__\\'\\__\\'\\__\\'\\_`.__|\"\"`. \\  \\___ \\\n"
                + "  \\\\/ __\\/ __\\/ __\\/ __\\/ __:                \\\n"
                + "   \\\\'\\__\\'\\__\\'\\__\\ \\__\\'\\_;-----------------`\n"
                + "    \\\\/   \\/   \\/   \\/   \\/ :               tk|\n"
                + "     \\|______________________;________________|\n";*/

        String title = "WELCOME TO SLACK MANAGEMENT PROGRAM!";
        int width = 60;
        String line = "-".repeat(width);

        //System.out.println(logo);
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
