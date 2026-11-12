package com.mycompany.quickchatpart3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

public class MessageRepositoryTest {

    @TempDir
    Path tempDir;

    private MessageRepository repository;

    private Message message1;
    private Message message2;
    private Message message3;
    private Message message4;
    private Message message5;

    @BeforeEach
    public void setUp() throws IOException {

        repository = new MessageRepository(
                tempDir.resolve("messages.json"));

        // Test Message 1 - Sent
        message1 = new Message(
                "0000000001",
                0,
                "Developer",
                "+27834557896",
                "Did you get the cake?"
        );

        // Test Message 2 - Stored
        message2 = new Message(
                "0000000002",
                1,
                "Developer",
                "+27838884567",
                "Where are you? You are late! I have asked you to be on time."
        );

        // Test Message 3 - Disregarded
        message3 = new Message(
                "0000000003",
                2,
                "Developer",
                "+27834484567",
                "Yohoooo, I am at your gate."
        );

        // Test Message 4 - Sent
        message4 = new Message(
                "0000000004",
                3,
                "Developer",
                "0838884567",
                "It is dinner time !"
        );

        // Test Message 5 - Stored
        message5 = new Message(
                "0000000005",
                4,
                "Developer",
                "+27838884567",
                "Ok, I am leaving without you."
        );

        repository.addMessage(message1, 1);
        repository.addMessage(message2, 3);
        repository.addMessage(message3, 2);
        repository.addMessage(message4, 1);
        repository.addMessage(message5, 3);
    }

    // TEST 1: Sent messages array
    @Test
    public void testSentMessagesArray() {

        List<String> expected = List.of(
                "Did you get the cake?",
                "It is dinner time !"
        );

        List<String> actual = repository
                .getSentMessages()
                .stream()
                .map(Message::getMessageText)
                .toList();

        assertEquals(expected, actual);
    }

    // TEST 2: Disregarded messages array
    @Test
    public void testDisregardedMessagesArray() {

        assertEquals(
                1,
                repository.getDisregardedMessages().size()
        );

        assertEquals(
                "Yohoooo, I am at your gate.",
                repository.getDisregardedMessages()
                        .get(0).getMessageText()
        );
    }

    // TEST 3: Stored messages array
    @Test
    public void testStoredMessagesArray() {

        assertEquals(
                2,
                repository.getStoredMessages().size()
        );
    }

    // TEST 4: Message IDs array
    @Test
    public void testMessageIDsArray() {

        assertEquals(
                5,
                repository.getMessageIDs().size()
        );

        assertTrue(
                repository.getMessageIDs()
                        .contains("0000000004")
        );
    }

    // TEST 5: Message hashes array
    @Test
    public void testMessageHashesArray() {

        assertEquals(
                5,
                repository.getMessageHashes().size()
        );

        assertTrue(
                repository.getMessageHashes()
                        .contains(message2.getMessageHash())
        );
    }

    // TEST 6: Display longest message
    @Test
    public void testLongestMessage() {

        String expected =
                "Where are you? You are late! I have asked you to be on time.";

        String actual = repository
                .getLongestMessage()
                .getMessageText();

        assertEquals(expected, actual);
    }

    // TEST 7: Search by message ID
    @Test
    public void testSearchMessageID() {

        Message result =
                repository.findByID("0000000004");

        assertNotNull(result);

        assertEquals(
                "It is dinner time !",
                result.getMessageText()
        );

        assertEquals(
                "0838884567",
                result.getRecipient()
        );
    }

    // TEST 8: Search by recipient
    @Test
    public void testSearchRecipient() {

        List<Message> results =
                repository.findByRecipient(
                        "+27838884567"
                );

        assertEquals(2, results.size());

        assertEquals(
                "Where are you? You are late! I have asked you to be on time.",
                results.get(0).getMessageText()
        );

        assertEquals(
                "Ok, I am leaving without you.",
                results.get(1).getMessageText()
        );
    }

    // TEST 9: Delete using message hash
    @Test
    public void testDeleteMessageHash() throws IOException {

        String hash = message2.getMessageHash();

        boolean deleted =
                repository.deleteByHash(hash);

        assertTrue(deleted);

        assertNull(
                repository.findByID(
                        message2.getMessageID()
                )
        );

        assertEquals(
                1,
                repository.getStoredMessages().size()
        );
    }

    // TEST 10: Read stored JSON file
    @Test
    public void testReadJSON() throws IOException {

        MessageRepository loaded =
                new MessageRepository(
                        tempDir.resolve("messages.json")
                );

        assertEquals(
                2,
                loaded.getStoredMessages().size()
        );

        assertEquals(
                message2.getMessageText(),
                loaded.getStoredMessages()
                        .get(0).getMessageText()
        );
    }

    // TEST 11: Full message report
    @Test
    public void testDisplayReport() {

        String report = repository.fullReport();

        assertTrue(
                report.contains(message1.getMessageHash())
        );

        assertTrue(
                report.contains("+27834557896")
        );

        assertTrue(
                report.contains("Did you get the cake?")
        );

        assertTrue(
                report.contains("It is dinner time !")
        );
    }

    // TEST 12: Sender and recipient details
    @Test
    public void testSenderAndRecipient() {

        String output =
                repository.storedSendersAndRecipients();

        assertTrue(
                output.contains("Sender: Developer")
        );

        assertTrue(
                output.contains("+27838884567")
        );
    }

    // TEST 13: Invalid message hash
    @Test
    public void testInvalidMessageHash() throws IOException {

        assertFalse(
                repository.deleteByHash(
                        "INVALID_HASH"
                )
        );
    }

    // TEST 14: Duplicate message ID
    @Test
    public void testDuplicateMessageID() {

        assertThrows(
                IllegalArgumentException.class,
                () -> repository.addMessage(message1, 1)
        );
    }
}
