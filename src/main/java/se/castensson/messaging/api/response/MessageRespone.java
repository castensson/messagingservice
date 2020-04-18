package se.castensson.messaging.api.response;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@RequiredArgsConstructor
public class MessageRespone {
    @NonNull
    private String text;
    @NonNull
    private UUID messageId;
    @NonNull
    private LocalDateTime received;
    @NonNull
    private String status;
}
