
package com.mycompany.quickchatpart3;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MessageRepository {

    // Reference: Google Gson User Guide
    // https://github.com/google/gson/blob/main/UserGuide.md
    private static final Gson GSON =
            new GsonBuilder().setPrettyPrinting().create();

    private static final Type MESSAGE_LIST =
            new TypeToken<ArrayList<Message>>() {}.getType();

    private final Path jsonFile;

    // Part 3: Five dynamically populated arrays.
    private final ArrayList<Message> sentMessages =
            new ArrayList<>();

    private final ArrayList<Message> disregardedMessages =
            new ArrayList<>();

    private final ArrayList<Message> storedMessages =
            new ArrayList<>();

    private final ArrayList<String> messageHashes =
            new ArrayList<>();

    private final ArrayList<String> messageIDs =
            new ArrayList<>();

    public MessageRepository(Path jsonFile) throws IOException {
        this.jsonFile = Objects.requireNonNull(jsonFile);
        loadStoredMessages();
    }

    // Read stored messages from messages.json.
    public final void loadStoredMessages() throws IOException {

        storedMessages.clear();

        if (Files.exists(jsonFile)) {

            String json = Files.readString(
                    jsonFile, StandardCharsets.UTF_8).trim();

            if (!json.isEmpty()) {

                try {
                    List<Message> items =
                            GSON.fromJson(json, MESSAGE_LIST);

                    if (items == null) {
                        throw new IOException(
                                "JSON must contain an array.");
                    }

                    for (Message message : items) {

                        if (message == null
                                || message.getMessageID() == null
                                || message.getMessageHash() == null) {

                            throw new IOException(
                                    "JSON contains an incomplete message.");
                        }

                        storedMessages.add(message);
                    }

                } catch (JsonSyntaxException
                         | IllegalStateException ex) {

                    throw new IOException(
                            "Could not read messages.json.", ex);
                }
            }
        }

        rebuildIndexes();
    }

    // Populate the message ID and message hash arrays.
    private void rebuildIndexes() {

        messageIDs.clear();
        messageHashes.clear();

        for (Message message : allMessages()) {
            messageIDs.add(message.getMessageID());
            messageHashes.add(message.getMessageHash());
        }
    }

    // Add a sent, disregarded, or stored message.
    public void addMessage(Message message, int selection)
            throws IOException {

        Objects.requireNonNull(message);

        if (!message.checkMessageID()
                || !"Cell phone number successfully captured."
                        .equals(message.checkRecipientCell())
                || !"Message ready to send."
                        .equals(message.checkMessageLength())) {

            throw new IllegalArgumentException(
                    "Invalid message details.");
        }

        if (messageIDs.contains(message.getMessageID())) {
            throw new IllegalArgumentException(
                    "Duplicate message ID.");
        }

        if (selection < 1 || selection > 3) {
            throw new IllegalArgumentException(
                    "Invalid selection.");
        }

        message.SentMessage(selection);

        switch (selection) {

            case 1 -> sentMessages.add(message);

            case 2 -> disregardedMessages.add(message);

            case 3 -> {
                storedMessages.add(message);

                try {
                    saveStoredMessages();
                } catch (IOException ex) {
                    storedMessages.remove(message);
                    throw ex;
                }
            }
        }

        rebuildIndexes();
    }

    // Save stored messages to the JSON file.
    public void saveStoredMessages() throws IOException {

        Path parent = jsonFile.toAbsolutePath().getParent();

        if (parent != null) {
            Files.createDirectories(parent);
        }

        Files.writeString(
                jsonFile,
                GSON.toJson(storedMessages, MESSAGE_LIST),
                StandardCharsets.UTF_8);
    }

    public List<Message> getSentMessages() {
        return Collections.unmodifiableList(sentMessages);
    }

    public List<Message> getDisregardedMessages() {
        return Collections.unmodifiableList(disregardedMessages);
    }

    public List<Message> getStoredMessages() {
        return Collections.unmodifiableList(storedMessages);
    }

    public List<String> getMessageHashes() {
        return Collections.unmodifiableList(messageHashes);
    }

    public List<String> getMessageIDs() {
        return Collections.unmodifiableList(messageIDs);
    }

    // Combine all messages for searching and reporting.
    public List<Message> allMessages() {

        ArrayList<Message> all = new ArrayList<>();

        all.addAll(sentMessages);
        all.addAll(disregardedMessages);
        all.addAll(storedMessages);

        return all;
    }

    // Find the longest message.
    public Message getLongestMessage() {

        Message longest = null;

        for (Message message : allMessages()) {

            if (longest == null
                    || message.getMessageText().length()
                    > longest.getMessageText().length()) {

                longest = message;
            }
        }

        return longest;
    }

    // Search using a message ID.
    public Message findByID(String id) {

        for (Message message : allMessages()) {

            if (message.getMessageID().equals(id)) {
                return message;
            }
        }

        return null;
    }

    // Search for messages belonging to a recipient.
    public List<Message> findByRecipient(String recipient) {

        ArrayList<Message> results = new ArrayList<>();

        for (Message message : allMessages()) {

            if (message.getRecipient().equals(recipient)) {
                results.add(message);
            }
        }

        return results;
    }

    // Delete a stored message using its hash.
    public boolean deleteByHash(String hash) throws IOException {

        for (int i = 0; i < storedMessages.size(); i++) {

            Message message = storedMessages.get(i);

            if (message.getMessageHash().equals(hash)) {

                storedMessages.remove(i);

                try {
                    saveStoredMessages();
                } catch (IOException ex) {
                    storedMessages.add(i, message);
                    throw ex;
                }

                rebuildIndexes();
                return true;
            }
        }

        return false;
    }

    // Display sender and recipient details.
    public String storedSendersAndRecipients() {

        if (storedMessages.isEmpty()) {
            return "No stored messages.";
        }

        StringBuilder result = new StringBuilder();

        for (Message message : storedMessages) {

            result.append("Sender: ")
                    .append(message.getSender())
                    .append(" | Recipient: ")
                    .append(message.getRecipient())
                    .append("\n");
        }

        return result.toString();
    }

    // Generate the complete message report.
    public String fullReport() {

        if (allMessages().isEmpty()) {
            return "No messages available.";
        }

        StringBuilder report =
                new StringBuilder("QUICKCHAT MESSAGE REPORT\n");

        for (Message message : allMessages()) {

            report.append("------------------------\n")
                    .append(message.printMessages())
                    .append("\n");
        }

        return report.toString();
    }
}
