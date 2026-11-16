package com.mycompany.quickchatpart3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import javax.swing.JOptionPane;

/** Programming 1A QuickChat: register, send, store, and manage messages. */
public class QuickChatPart3 {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path JSON_PATH = Path.of("messages.json");
    private static final Random RANDOM = new Random();
    private static final List<ChatMessage> allMessages = new ArrayList<>();
    private static final List<ChatMessage> sentMessages = new ArrayList<>();
    private static final List<ChatMessage> disregardedMessages = new ArrayList<>();
    private static final List<ChatMessage> storedMessages = new ArrayList<>();
    private static final List<String> messageHashes = new ArrayList<>();
    private static final List<String> messageIDs = new ArrayList<>();
    private static String username;
    private static String password;
    private static String phone;
    private static int messageLimit;
    private static int createdCount;

    public static void main(String[] args) {
        JOptionPane.showMessageDialog(null, "Welcome to QuickChat.");
        if (!register() || !login()) return;
        loadStoredMessages();
        String limit = JOptionPane.showInputDialog(null, "How many messages would you like to create?");
        if (limit == null) return;
        try {
            messageLimit = Integer.parseInt(limit.trim());
            if (messageLimit < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            show("Please enter a valid non-negative whole number.");
            return;
        }
        mainMenu();
    }

    private static boolean register() {
        while (true) {
            username = JOptionPane.showInputDialog(null, "Register: username (underscore, maximum 5 characters):");
            if (username == null) return false;
            if (username.matches("(?=.{1,5}$)[A-Za-z0-9_]*_[A-Za-z0-9_]*")) break;
            show("Username must contain an underscore and be at most 5 characters.");
        }
        while (true) {
            password = JOptionPane.showInputDialog(null, "Register: password (8+ chars, upper, lower, digit, special):");
            if (password == null) return false;
            if (password.matches("(?s)^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,}$")) break;
            show("Password does not meet the requirements.");
        }
        while (true) {
            phone = JOptionPane.showInputDialog(null, "Register: SA cellphone (+27 followed by 9 digits):");
            if (phone == null) return false;
            if (validPhone(phone)) break;
            show("Invalid South African cellphone number.");
        }
        show("Registration successful.");
        return true;
    }

    private static boolean login() {
        for (int i = 0; i < 3; i++) {
            String name = JOptionPane.showInputDialog(null, "Login: username:");
            if (name == null) return false;
            String pass = JOptionPane.showInputDialog(null, "Login: password:");
            if (pass == null) return false;
            if (username.equals(name) && password.equals(pass)) {
                show("Login successful.");
                return true;
            }
            show("Incorrect username or password.");
        }
        show("Login failed. Goodbye.");
        return false;
    }

    private static void mainMenu() {
        while (true) {
            String choice = JOptionPane.showInputDialog(null,
                    "QUICKCHAT MENU\n1) Send Messages\n2) Show recently sent messages\n3) Quit\n4) Stored Messages / Message Manager\n\nSelect 1-4:");
            if (choice == null || choice.trim().equals("3")) break;
            switch (choice.trim()) {
                case "1" -> composeMessage();
                case "2" -> showRecentMessages(); // Replaces the old Coming Soon placeholder.
                case "4" -> messageManager();
                default -> show("Please choose 1, 2, 3 or 4.");
            }
        }
        show("Total messages sent: " + sentMessages.size() + "\nGoodbye.");
    }

    private static void composeMessage() {
        if (createdCount >= messageLimit) {
            show("Message limit reached (" + messageLimit + ").");
            return;
        }
        String recipient = JOptionPane.showInputDialog(null, "Recipient (+27 followed by 9 digits):");
        if (recipient == null) return;
        if (!validPhone(recipient)) {
            show("Cell phone number is incorrectly formatted or does not contain an international code. Please correct the number and try again.");
            return;
        }
        String body = JOptionPane.showInputDialog(null, "Message (1-250 characters):");
        if (body == null) return;
        if (body.isBlank() || body.length() > 250) {
            show(body.length() > 250 ? "Message exceeds 250 characters by " + (body.length() - 250) + "; please reduce the size." : "Please enter a message of less than 250 characters.");
            return;
        }
        String id = generateMessageID();
        int sequence = createdCount + 1;
        String hash = createHash(id, sequence, body);
        String action = JOptionPane.showInputDialog(null, "Message ID: " + id + "\nHash: " + hash
                + "\n1) Send Message\n2) Disregard Message\n3) Store Message for later");
        if (action == null || !List.of("1", "2", "3").contains(action.trim())) return;
        ChatMessage message = new ChatMessage(id, hash, phone, recipient, body);
        createdCount++;
        switch (action.trim()) {
            case "1" -> {
                message.status = "Sent";
                addMessage(message);
                show("Message successfully sent.\n" + details(message));
            }
            case "2" -> {
                message.status = "Disregarded";
                addMessage(message);
                show("Message disregarded.");
            }
            case "3" -> {
                message.status = "Stored";
                addMessage(message);
                saveStoredMessages();
                show("Message successfully stored.\n" + details(message));
            }
        }
    }

    /** Populate the in-memory arrays once per message. */
    private static void addMessage(ChatMessage message) {
        allMessages.add(message);
        messageIDs.add(message.id);
        messageHashes.add(message.hash);
        switch (message.status) {
            case "Sent" -> sentMessages.add(message);
            case "Disregarded" -> disregardedMessages.add(message);
            case "Stored" -> storedMessages.add(message);
        }
    }

    private static void showRecentMessages() {
        if (sentMessages.isEmpty()) {
            show("No sent messages yet.");
            return;
        }
        StringBuilder report = new StringBuilder("RECENTLY SENT MESSAGES\n");
        for (ChatMessage m : sentMessages) report.append("\n").append(details(m)).append("\n");
        show(report.toString());
    }

    private static void messageManager() {
        while (true) {
            String choice = JOptionPane.showInputDialog(null,
                    "STORED MESSAGES / MESSAGE MANAGER\n1) Show stored messages\n2) Longest stored message\n3) Find message by ID\n4) Search by recipient\n5) Delete stored message by hash\n6) Display full report\n7) Reload JSON stored messages\n8) Back to main menu");
            if (choice == null || choice.trim().equals("8")) return;
            switch (choice.trim()) {
                case "1" -> showStoredMessages();
                case "2" -> showLongestStored();
                case "3" -> searchByID();
                case "4" -> searchByRecipient();
                case "5" -> deleteByHash();
                case "6" -> showFullReport();
                case "7" -> { loadStoredMessages(); show("Stored messages loaded from JSON."); }
                default -> show("Choose a number from 1 to 8.");
            }
        }
    }

    private static void showStoredMessages() {
        StringBuilder report = new StringBuilder("STORED MESSAGES (sender and recipient)\n");
        for (ChatMessage m : storedMessages) report.append("\nSender: ").append(m.sender)
                .append("\nRecipient: ").append(m.recipient).append("\n").append(details(m)).append("\n");
        show(storedMessages.isEmpty() ? "No stored messages." : report.toString());
    }

    private static void showLongestStored() {
        ChatMessage longest = storedMessages.stream().max(Comparator.comparingInt(m -> m.body.length())).orElse(null);
        show(longest == null ? "No stored messages." : "Longest stored message:\n" + details(longest));
    }

    private static void searchByID() {
        String search = JOptionPane.showInputDialog(null, "Enter message ID:");
        if (search == null) return;
        for (ChatMessage m : allMessages) if (m.id.equals(search.trim())) { show(details(m)); return; }
        show("Message ID not found.");
    }

    private static void searchByRecipient() {
        String recipient = JOptionPane.showInputDialog(null, "Enter recipient phone number:");
        if (recipient == null) return;
        StringBuilder report = new StringBuilder("Messages for " + recipient.trim() + ":\n");
        int found = 0;
        for (ChatMessage m : storedMessages) if (m.recipient.equals(recipient.trim())) {
            report.append("\n").append(details(m)).append("\n");
            found++;
        }
        show(found == 0 ? "No stored messages found for that recipient." : report.toString());
    }

    private static void deleteByHash() {
        String hash = JOptionPane.showInputDialog(null, "Enter stored message hash to delete:");
        if (hash == null) return;
        for (ChatMessage m : new ArrayList<>(storedMessages)) {
            if (m.hash.equalsIgnoreCase(hash.trim())) {
                storedMessages.remove(m);
                allMessages.remove(m);
                messageHashes.remove(m.hash);
                messageIDs.remove(m.id);
                saveStoredMessages();
                show("Message successfully deleted.");
                return;
            }
        }
        show("Message hash not found.");
    }

    private static void showFullReport() {
        if (allMessages.isEmpty()) { show("Report is empty."); return; }
        StringBuilder report = new StringBuilder("FULL MESSAGE REPORT\n");
        for (ChatMessage m : allMessages) report.append("\n").append(details(m)).append("\n");
        show(report.toString());
    }

    private static String details(ChatMessage m) {
        return "ID: " + m.id + "\nHash: " + m.hash + "\nSender: " + m.sender
                + "\nRecipient: " + m.recipient + "\nMessage: " + m.body + "\nStatus: " + m.status;
    }

    private static void loadStoredMessages() {
        if (!Files.exists(JSON_PATH)) return;
        try (Reader reader = Files.newBufferedReader(JSON_PATH)) {
            Type type = new TypeToken<ArrayList<ChatMessage>>() {}.getType();
            List<ChatMessage> saved = GSON.fromJson(reader, type);
            if (saved != null) for (ChatMessage m : saved) {
                if (m != null && m.id != null && m.hash != null && m.recipient != null && m.body != null
                        && allMessages.stream().noneMatch(old -> old.id.equals(m.id))) {
                    m.status = "Stored";
                    addMessage(m);
                }
            }
        } catch (IOException | RuntimeException ex) {
            show("Unable to read JSON: " + ex.getMessage());
        }
    }

    private static void saveStoredMessages() {
        try (Writer writer = Files.newBufferedWriter(JSON_PATH)) {
            GSON.toJson(storedMessages, writer);
        } catch (IOException ex) {
            show("Unable to write JSON: " + ex.getMessage());
        }
    }

    public static boolean validPhone(String number) { return number != null && number.matches("\\+27[0-9]{9}"); }

    public static String createHash(String id, int sequence, String body) {
        String[] words = body.trim().split("\\s+");
        return (id.substring(0, 2) + ":" + sequence + ":" + words[0] + words[words.length - 1]).toUpperCase();
    }

    private static String generateMessageID() {
        String id;
        do { id = String.format("%010d", RANDOM.nextLong(10_000_000_000L)); }
        while (messageIDs.contains(id));
        return id;
    }

    private static void show(String text) { JOptionPane.showMessageDialog(null, text); }

    /** Represents one message stored in the report or the JSON file. */
    private static final class ChatMessage {
        String id, hash, sender, recipient, body, status;
        ChatMessage(String id, String hash, String sender, String recipient, String body) {
            this.id = id; this.hash = hash; this.sender = sender; this.recipient = recipient; this.body = body;
        }
    }
}
