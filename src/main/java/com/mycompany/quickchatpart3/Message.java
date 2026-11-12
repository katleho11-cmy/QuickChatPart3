
package com.mycompany.quickchatpart3;

import java.security.SecureRandom;
import java.util.Locale;

public class Message {

    private static final SecureRandom RANDOM = new SecureRandom();

    private String messageID;
    private int sequence;
    private String sender;
    private String recipient;
    private String messageText;
    private String messageHash;
    private String status;

    private Message() {
        // Used when reading messages from JSON.
    }

    public Message(int sequence, String recipient,
                   String messageText) {
        this(String.format("%010d",
                RANDOM.nextLong(10_000_000_000L)),
                sequence, "Current user",
                recipient, messageText);
    }

    public Message(String id, int sequence,
                   String recipient, String messageText) {
        this(id, sequence, "Current user",
                recipient, messageText);
    }

    public Message(String id, int sequence,
                   String sender, String recipient,
                   String messageText) {

        this.messageID = id;
        this.sequence = sequence;
        this.sender = sender;
        this.recipient = recipient;
        this.messageText = messageText;
        this.messageHash = createMessageHash();
        this.status = "Created";
    }

    public boolean checkMessageID() {
        return messageID != null
                && messageID.matches("[0-9]{10}");
    }

    public String checkRecipientCell() {
        if (recipient != null &&
                (recipient.matches("\\+27[0-9]{9}")
                || recipient.matches("0[0-9]{9}"))) {
            return "Cell phone number successfully captured.";
        }

        return "Cell phone number is incorrectly formatted. "
                + "Use +27 followed by 9 digits "
                + "(or 0 followed by 9 digits).";
    }

    public String checkMessageLength() {
        if (messageText != null
                && !messageText.isBlank()
                && messageText.length() <= 250) {
            return "Message ready to send.";
        }

        return "Please enter a message of less than 250 characters.";
    }

    public String createMessageHash() {
        if (messageText == null
                || messageText.isBlank()
                || messageID == null
                || messageID.length() < 2) {
            return "";
        }

        String[] words = messageText.trim().split("\\s+");

        String first = words[0].replaceAll(
                "^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");

        String last = words[words.length - 1].replaceAll(
                "^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");

        return (messageID.substring(0, 2)
                + ":" + sequence + ":"
                + first + last).toUpperCase(Locale.ROOT);
    }

    public String SentMessage(int selection) {
        switch (selection) {
            case 1 -> {
                status = "Sent";
                return "Message successfully sent.";
            }
            case 2 -> {
                status = "Disregarded";
                return "Message disregarded.";
            }
            case 3 -> {
                status = "Stored";
                return "Message successfully stored.";
            }
            default -> {
                return "Invalid selection.";
            }
        }
    }

    public String printMessages() {
        return "Sender: " + sender
                + "\nMessage ID: " + messageID
                + "\nMessage Hash: " + messageHash
                + "\nRecipient: " + recipient
                + "\nMessage: " + messageText
                + "\nStatus: " + status;
    }

    public String getMessageID() {
        return messageID;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getMessageText() {
        return messageText;
    }

    public String getMessageHash() {
        return messageHash;
    }

    public String getSender() {
        return sender;
    }

    public String getStatus() {
        return status;
    }

    public int getSequence() {
        return sequence;
    }
}
