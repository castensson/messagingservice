package se.castensson.messaging.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Set;

@Data
public class DeleteRequest {
    @JsonProperty("messages")
    private Set<String> messages;

    @JsonProperty("userId")
    private String userId;
}
