package com.kahootclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

public class Client {
    private static final String COMMAND_PREFIX = "/";
    private static final String IP_ADDRESS = "127.0.0.1";
    private static final int PORT_NUMBER = 6000;

    /**
     * regex pattern that only permits letters and numbers.
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9]*$");

    public void start() {
        try (Socket clientSocket = new Socket(IP_ADDRESS, PORT_NUMBER);
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))) {

            String username = readUsername();
            out.println("h" + username);

            Thread messageReaderThread = new Thread(() -> readServerResponse(in));
            messageReaderThread.start();

            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                if (userInput.startsWith(COMMAND_PREFIX)) {
                    handleCommand(userInput.substring(1));
                } else {
                    out.println(username + ": " + userInput);
                }
            }
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + IP_ADDRESS);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Server probably isn't running on " + IP_ADDRESS);
            System.exit(1);
        }
    }



    /**
     * Reads the username from the user.
     *
     * @return the username entered by the user
     * @throws IOException if an I/O error occurs while reading the input
     */
    private String readUsername() throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String username;
        do {
            System.out.println("Enter your username");
            username = reader.readLine();
        } while (!validateUsername(username));
        return username;
    }

    /**
     * Validates the username for length and special characters.
     *
     * @param username the username to validate
     * @return true if the username is valid, false otherwise
     */
    private boolean validateUsername(String username) {
        if (username.length() <= 5 || username.length() >= 20 || !USERNAME_PATTERN.matcher(username).matches()) {
            System.err.println("The username must have between 5 and 20 characters. ");
            return false;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            System.err.println("The username can't contain any special characters. ");
            return false;
        }
        return true;
    }


    /**
     * Reads and displays server responses.
     *
     * @param in the reader to read server responses from
     */
    private void readServerResponse(BufferedReader in) {
        try {
            String serverResponse;
            while ((serverResponse = in.readLine()) != null) {
                System.out.println(serverResponse.substring(1));
            }
        } catch (IOException e) {
            System.err.println("Error reading server response: " + e.getMessage());
        }
    }

    /**
     * Handles a command received from the user.
     *
     * @param command the command to handle
     */
    private void handleCommand(String command) {
        switch (command) {
            case "exit" -> {
                System.out.println("Connection is being aborted...");
                System.exit(0);
            }
            default -> System.out.println("Unknown command: " + "\"" + command + "\"");
        }
    }
}