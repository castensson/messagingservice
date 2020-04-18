package se.castensson.messaging.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.castensson.messaging.api.request.MessageRequest;
import se.castensson.messaging.api.response.MessageRespone;
import se.castensson.messaging.api.response.StatusResponse;
import se.castensson.messaging.exceptions.MessageBoxIndexException;
import se.castensson.messaging.exceptions.MessageBoxMissingException;
import se.castensson.messaging.repository.Message;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class MessagingServiceTest {

    public static final String DEFAULT_USER = "EXISTING_USER";
    MessagingService messagingService = new MessagingService();
    MessageRequest defaultMessageRequest = new MessageRequest(DEFAULT_USER, "text");

    Message defaultMessage = null;

    @BeforeEach
    void setup() {
        this.messagingService = new MessagingService();
        defaultMessage = this.messagingService.storeMessage(defaultMessageRequest);
    }

    @Test
    void storeMessage() {
        MessageRequest request = new MessageRequest("user", "text");
        Message message = this.messagingService.storeMessage(request);
        assertThat(message)
                .isNotNull()
                .hasFieldOrPropertyWithValue("messageStatus", Message.Status.UNFETCHED)
                .hasFieldOrPropertyWithValue("text", "text");
    }

    @Test
    void getUnreadMessages() throws MessageBoxMissingException {
        // Should contain default entry
        assertThat(messagingService.getUnreadMessages(DEFAULT_USER)).isNotEmpty().contains(defaultMessage.marshall());
        // Should now be empty
        assertThat(messagingService.getUnreadMessages(DEFAULT_USER)).isEmpty();
    }

    @Test
    void deleteMessages() throws MessageBoxMissingException {
        StatusResponse statusResponse = messagingService.getStatus(DEFAULT_USER);
        // Should have one un-fetched message
        assertThat(statusResponse)
                .hasFieldOrPropertyWithValue("fetched", 0L)
                .hasFieldOrPropertyWithValue("unfetched", 1L);
        messagingService.deleteMessages(DEFAULT_USER, Set.of((defaultMessage.getUuid().toString())));
        statusResponse = messagingService.getStatus(DEFAULT_USER);
        // Should now have no messages
        assertThat(statusResponse)
                .hasFieldOrPropertyWithValue("fetched", 0L)
                .hasFieldOrPropertyWithValue("unfetched", 0L);

    }

    @Test
    void getStatus() throws MessageBoxMissingException {
        StatusResponse statusResponse = messagingService.getStatus(DEFAULT_USER);
        // Should have one un-fetched
        assertThat(statusResponse)
                .hasFieldOrPropertyWithValue("fetched", 0L)
                .hasFieldOrPropertyWithValue("unfetched", 1L);
        messagingService.getUnreadMessages(DEFAULT_USER);
        statusResponse = messagingService.getStatus(DEFAULT_USER);
        // Should have one fetched
        assertThat(statusResponse)
                .hasFieldOrPropertyWithValue("fetched", 1L)
                .hasFieldOrPropertyWithValue("unfetched", 0L);


    }

    @Test
    void validateMessageBoxExists() {
        // Test non existing
        assertThrows(MessageBoxMissingException.class, () -> messagingService.validateMessageBoxExists("NON_EXISTING_USER"));
        // Test existing
        assertDoesNotThrow(() -> messagingService.validateMessageBoxExists(DEFAULT_USER));
    }

    @Test
    void getMessagesByIndex() throws MessageBoxMissingException, MessageBoxIndexException {
        // Index Validation
        assertThrows(MessageBoxIndexException.class, () -> messagingService.getMessagesByIndex(DEFAULT_USER, 5,1));
        assertThrows(MessageBoxIndexException.class, () -> messagingService.getMessagesByIndex(DEFAULT_USER, -5,-10));
        assertThrows(MessageBoxIndexException.class, () -> messagingService.getMessagesByIndex(DEFAULT_USER, 5,-10));
        assertThrows(MessageBoxIndexException.class, () -> messagingService.getMessagesByIndex(DEFAULT_USER, -5,10));
        // Start index > size of message box
        assertThrows(MessageBoxIndexException.class, () -> messagingService.getMessagesByIndex(DEFAULT_USER, 5,10));
        // Should get the default message
        List<MessageRespone> messages =  messagingService.getMessagesByIndex(DEFAULT_USER, 0,1);
        assertThat(messages).containsExactly(defaultMessage.marshall());
        // Let's add a few more messages
        insertMessages(50);
        // Verify that 10 is retrieved
        messages =  messagingService.getMessagesByIndex(DEFAULT_USER, 15,25);
        assertThat(messages).size().isEqualTo(10);
        // Verify that 5 is retrieved
        messages =  messagingService.getMessagesByIndex(DEFAULT_USER, 45,50);
        assertThat(messages).size().isEqualTo(5);
        // Verify sorted order
        assertThat(messages).isSortedAccordingTo(Comparator.comparing(MessageRespone::getReceived));

    }

    private void insertMessages(int nrOfMessges) {
        for (int i = 0; i < nrOfMessges; i++){
            MessageRequest message = new MessageRequest(DEFAULT_USER, "message " + i);
            messagingService.storeMessage(message);
        }
    }


}