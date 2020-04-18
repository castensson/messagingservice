package se.castensson.messaging.repository;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import se.castensson.messaging.api.response.MessageRespone;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class Message {
    public enum Status {FETCHED, UNFETCHED}

    private UUID uuid = UUID.randomUUID();
    private Status messageStatus = Status.UNFETCHED;
    @NonNull
    private LocalDateTime received;
    @NonNull
    private String text;

    public MessageRespone marshall() {
        return new MessageRespone(this.text, this.getUuid(), received, messageStatus.toString());
    }
}
