
package com.mycompany.quickchatpart3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JOptionPane;

public class QuickChatPart3 {

    private static MessageRepository repository;

    private static String accountName;
    private static String accountPassword;

    private static int messageSequence = 0;
    private static int maximumMessages = 0;

    public static void main(String[] args) {

        try {
            repository = new MessageRepository(
                    Path.of("messages.json"));

        } catch (IOException ex) {
            show("Could not load stored messages: "
                    + ex.getMessage());
            return;
        }

        if (!createAccount() || !signIn()) {
            return;
        }

        show("Welcome to QuickChat.");

        Integer count = askNumber(
                "How many messages would you like to enter?",
                0, 100000);

        if (count == null) {
            return;
        }

        maximumMessages = count;

        showMenu();
    }

    // PART 1: Register a user account.
    private static boolean createAccount() {

        while (true) {

            accountName = JOptionPane.showInputDialog(
                    null,
                    "Create username (underscore and at most 5 characters):");

            if (accountName == null) {
                return false;
            }

            if (accountName.contains("_")
                    && accountName.length() <= 5) {
                break;
            }

            show("Username must contain _ "
                    + "and have at most 5 characters.");
        }

        while (true) {

            accountPassword = JOptionPane.showInputDialog(
                    null,
                    "Create password (8+ characters, capital, digit, special):");

            if (accountPassword == null) {
                return false;
            }

            if (accountPassword.matches(
                    "(?s)^(?=.*[A-Z])(?=.*[0-9])"
                    + "(?=.*[^a-zA-Z0-9]).{8,}$")) {
                break;
            }

            show("Password does not meet the requirements.");
        }

        while (true) {

            String phone = JOptionPane.showInputDialog(
                    null,
                    "Enter SA cellphone (+27 followed by 9 digits):");

            if (phone == null) {
                return false;
            }

            if (phone.matches("\\+27[0-9]{9}")) {
                break;
            }

            show("Invalid international SA cellphone number.");
        }

        show("Registration successful.");
        return true;
    }

    // PART 1: Log in to QuickChat.
    private static boolean signIn() {

        for (int attempts = 0; attempts < 3; attempts++) {

            String username = JOptionPane.showInputDialog(
                    null, "Login username:");

            if (username == null) {
                return false;
            }

            String password = JOptionPane.showInputDialog(
                    null, "Login password:");

            if (password == null) {
                return false;
            }

            if (accountName.equals(username)
                    && accountPassword.equals(password)) {

                show("Login successful.");
                return true;
            }

            show("Incorrect username or password.");
        }

        return false;
    }

    // PART 3: Main menu with the fourth option.
    private static void showMenu() {

        while (true) {

            String choice = JOptionPane.showInputDialog(
                    null,
                    "1) Send Messages"
                    + "\n2) Show recently sent messages"
                    + "\n3) Quit"
                    + "\n4) Stored Messages"
                    + "\n\nChoose 1-4:");

            if (choice == null
                    || choice.trim().equals("3")) {
                break;
            }

            switch (choice.trim()) {

                case "1" -> composeMessage();

                case "2" -> show("Coming Soon.");

                case "4" -> storedMenu();

                default -> show("Choose 1, 2, 3 or 4.");
            }
        }

        show("Total messages sent: "
                + repository.getSentMessages().size());
    }

    // PART 2: Compose and process a message.
    private static void composeMessage() {

        if (messageSequence >= maximumMessages) {
            show("You have reached your message limit.");
            return;
        }

        String recipient = JOptionPane.showInputDialog(
                null, "Recipient (+27 followed by 9 digits):");

        if (recipient == null) {
            return;
        }

        String body = JOptionPane.showInputDialog(
                null, "Write a message (maximum 250 characters):");

        if (body == null) {
            return;
        }

        Message draft = new Message(
                messageSequence, recipient, body);

        // Keep the generated ID and set the sender.
        draft = new Message(
                draft.getMessageID(),
                messageSequence,
                accountName,
                recipient,
                body
        );

        if (!draft.checkMessageID()) {
            show("Unable to generate a valid message ID.");
            return;
        }

        if (!"Cell phone number successfully captured."
                .equals(draft.checkRecipientCell())) {

            show(draft.checkRecipientCell());
            return;
        }

        if (!"Message ready to send."
                .equals(draft.checkMessageLength())) {

            show(draft.checkMessageLength());
            return;
        }

        Integer selection = askNumber(
                "1) Send Message"
                + "\n2) Disregard Message"
                + "\n3) Store Message to send later",
                1, 3);

        if (selection == null) {
            return;
        }

        if (selection == 2) {

            String confirm = JOptionPane.showInputDialog(
                    null, "Press 0 to disregard the message:");

            if (!"0".equals(confirm)) {
                return;
            }
        }

        try {
            repository.addMessage(draft, selection);
            messageSequence++;

            switch (selection) {

                case 1 -> {
                    show(draft.printMessages());
                    show("Message sent");
                }

                case 2 -> show("Message disregarded.");

                case 3 -> show("Message successfully stored.");
            }

        } catch (IOException | IllegalArgumentException ex) {
            show("Could not process message: "
                    + ex.getMessage());
        }
    }

    // PART 3: Stored Messages submenu.
    private static void storedMenu() {

        while (true) {

            String choice = JOptionPane.showInputDialog(
                    null,
                    "STORED MESSAGES"
                    + "\n1) Display sender and recipients"
                    + "\n2) Display longest message"
                    + "\n3) Search by message ID"
                    + "\n4) Search by recipient"
                    + "\n5) Delete by message hash"
                    + "\n6) Display full report"
                    + "\n7) Reload stored JSON messages"
                    + "\n8) Back to main menu");

            if (choice == null
                    || choice.trim().equals("8")) {
                return;
            }

            switch (choice.trim()) {

                case "1" -> show(
                        repository.storedSendersAndRecipients());

                case "2" -> {

                    Message longest =
                            repository.getLongestMessage();

                    show(longest == null
                            ? "No messages available."
                            : longest.printMessages());
                }

                case "3" -> {

                    String id = JOptionPane.showInputDialog(
                            null, "Enter message ID:");

                    if (id == null) {
                        break;
                    }

                    Message found =
                            repository.findByID(id.trim());

                    show(found == null
                            ? "Message not found."
                            : found.printMessages());
                }

                case "4" -> {

                    String recipient =
                            JOptionPane.showInputDialog(
                                    null, "Enter recipient:");

                    if (recipient == null) {
                        break;
                    }

                    List<Message> matches =
                            repository.findByRecipient(
                                    recipient.trim());

                    if (matches.isEmpty()) {
                        show("No messages found.");

                    } else {

                        StringBuilder result =
                                new StringBuilder();

                        for (Message message : matches) {
                            result.append(message.printMessages())
                                    .append("\n\n");
                        }

                        show(result.toString());
                    }
                }

                case "5" -> {

                    String hash = JOptionPane.showInputDialog(
                            null, "Enter message hash to delete:");

                    if (hash == null) {
                        break;
                    }

                    try {
                        boolean deleted =
                                repository.deleteByHash(
                                        hash.trim());

                        show(deleted
                                ? "Message successfully deleted."
                                : "Stored message hash not found.");

                    } catch (IOException ex) {
                        show("Could not update JSON: "
                                + ex.getMessage());
                    }
                }

                case "6" -> show(repository.fullReport());

                case "7" -> {

                    try {
                        repository.loadStoredMessages();
                        show("Stored messages loaded from JSON.");

                    } catch (IOException ex) {
                        show("Could not read JSON: "
                                + ex.getMessage());
                    }
                }

                default -> show("Choose 1 to 8.");
            }
        }
    }

    // Helper method for numeric input.
    private static Integer askNumber(
            String question, int minimum, int maximum) {

        while (true) {

            String input = JOptionPane.showInputDialog(
                    null, question);

            if (input == null) {
                return null;
            }

            try {
                int number = Integer.parseInt(
                        input.trim());

                if (number >= minimum
                        && number <= maximum) {
                    return number;
                }

            } catch (NumberFormatException ignored) {
                // Display the validation message below.
            }

            show("Enter a number between "
                    + minimum + " and " + maximum + ".");
        }
    }

    private static void show(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
