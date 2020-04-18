package se.castensson.messaging.api.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusResponse {
    private Long fetched;
    private Long unfetched;
}
