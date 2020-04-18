package se.castensson.messaging.model;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;


@Data
@RequiredArgsConstructor()
public class MessageBox {
    @NonNull
    private String userId;
    private List<Message> messages = new LinkedList<>();

}
