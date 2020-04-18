package se.castensson.messaging.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import se.castensson.messaging.api.request.MessageRequest;
import se.castensson.messaging.api.response.MessageRespone;
import se.castensson.messaging.api.response.StatusResponse;
import se.castensson.messaging.exceptions.MessageBoxIndexException;
import se.castensson.messaging.exceptions.MessageBoxMissingException;
import se.castensson.messaging.model.Message;
import se.castensson.messaging.model.MessageBox;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomUtils;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static se.castensson.messaging.model.Message.Status.FETCHED;
import static se.castensson.messaging.model.Message.Status.UNFETCHED;

@Service
@Slf4j
public class MessagingService {

    private Map<String, MessageBox> storage = new HashMap<>();

    public Message storeMessage(MessageRequest messageRequest){

        if(!storage.containsKey(messageRequest.getUserId())){
            storage.put(messageRequest.getUserId(), new MessageBox(messageRequest.getUserId()));
        }

        MessageBox messageBox = storage.get(messageRequest.getUserId());
        // Manipulate time to get variation in received time
        Message message = new Message(LocalDateTime.now()
                .minusMinutes(RandomUtils.nextInt(0, 30))
                .minusHours(RandomUtils.nextInt(0,6)),
                messageRequest.getText());

        messageBox.getMessages().add(message);
        return message;
    }

    public List<MessageRespone> getUnreadMessages(String userId) throws MessageBoxMissingException {

        validateMessageBoxExists(userId);

        return storage.get(userId).getMessages()
        .stream()
        .filter(message -> !(message.getMessageStatus() == FETCHED))
        .map(message -> {
            message.setMessageStatus(FETCHED);
            return message.marshall();
        })
        .collect(Collectors.toList());
    }

    public void deleteMessages(String userId, Set<String> messages) throws MessageBoxMissingException {

        validateMessageBoxExists(userId);

        List<Message> operatedList = new ArrayList<>();
        storage.get(userId).getMessages().stream()
            .filter(message -> messages.contains(message.getUuid().toString()))
            .forEach(operatedList::add);
        log.info("Removing {} messages", operatedList.size());
        storage.get(userId).getMessages().removeAll(operatedList);
    }

    public StatusResponse getStatus(String userId) throws MessageBoxMissingException {
        validateMessageBoxExists(userId);

        Map<Message.Status, Long> statusMap = storage.get(userId).getMessages()
                .stream()
                .collect(groupingBy(Message::getMessageStatus, counting()));

        return new StatusResponse(
                statusMap.getOrDefault(FETCHED, 0L),
                statusMap.getOrDefault(UNFETCHED, 0L)
        );
    }

    void validateMessageBoxExists(String userId) throws MessageBoxMissingException {
        if(!storage.containsKey(userId)){
            throw new MessageBoxMissingException("No messageBox found for " + userId);
        }
    }

    public List<MessageRespone> getMessagesByIndex(String userId, Integer startIndex, Integer stopIndex)
            throws MessageBoxMissingException, MessageBoxIndexException {
        validateMessageBoxExists(userId);
        validatedIndexes(startIndex, stopIndex);

        List<Message> messages = storage.get(userId).getMessages();
        if(messages.size() < startIndex){
            throw new MessageBoxIndexException("Start index higher that available number of messages");
        }

        return messages
                .stream()
                .sorted(Comparator.comparing(Message::getReceived))
                .map(Message::marshall)
            .   collect(Collectors.toList())
                .subList(startIndex, stopIndex <= messages.size() ? stopIndex : messages.size());
    }

    void validatedIndexes(Integer startIndex, Integer stopIndex) throws MessageBoxIndexException {
        if(startIndex < 0 || stopIndex < 0 || stopIndex < startIndex){
            throw new MessageBoxIndexException("Invalid indexes");
        }
    }
}
