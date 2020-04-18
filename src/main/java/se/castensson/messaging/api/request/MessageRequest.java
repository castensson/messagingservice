package se.castensson.messaging.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageRequest {

    @JsonProperty("userId")
    private String userId;
    @JsonProperty("text")
    private String text;

}
