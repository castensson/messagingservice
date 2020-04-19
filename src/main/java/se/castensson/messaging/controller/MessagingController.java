package se.castensson.messaging.controller;

import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.annotations.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.castensson.messaging.api.request.DeleteRequest;
import se.castensson.messaging.api.request.MessageRequest;
import se.castensson.messaging.api.response.MessageRespone;
import se.castensson.messaging.api.response.StatusResponse;
import se.castensson.messaging.exceptions.MessageBoxIndexException;
import se.castensson.messaging.exceptions.MessageBoxMissingException;
import se.castensson.messaging.service.MessagingService;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.Callable;

@Slf4j
@RestController
@RequestMapping("api/messaging")
@Api
public class MessagingController {

    private final MessagingService messagingService;
    private final MeterRegistry registry;

    public MessagingController(MessagingService messagingService, MeterRegistry registry) {
        this.messagingService = messagingService;
        this.registry = registry;
    }

    @PostMapping
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Messages received", response = MessageRespone.class)
    })
    @ApiOperation(value = "Post new messages", response = HttpStatus.class)
    public Callable<ResponseEntity<HttpStatus>> postMessage(@RequestBody @Valid MessageRequest messageRequest){
        registry.counter("messages_received").increment();
        return () -> {
            log.info("Message received: " + messagingService.storeMessage(messageRequest));
            return new ResponseEntity<>(HttpStatus.CREATED);
        };
    }

    @RequestMapping(
            value = "unread/{userId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Messages retrieved", response = MessageRespone.class),
            @ApiResponse(code = 404, message = "No message box found for user", response = HttpStatus.class)
    })
    @ApiOperation(value = "Retrieve all unread messages, will be marked as read", response = HttpStatus.class)
    public Callable<ResponseEntity<List<MessageRespone>>> getUnread(@PathVariable("userId") String userId){
        return () -> {
            try {
              return new ResponseEntity<>(messagingService.getUnreadMessages(userId), HttpStatus.OK);
            } catch (MessageBoxMissingException me){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }

    @RequestMapping(
            value = "messages/{userId}/{startIndex}/{stopIndex}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Messages retrieved", response = MessageRespone.class),
            @ApiResponse(code = 404, message = "No message box found for user", response = HttpStatus.class),
            @ApiResponse(code = 400, message = "Invalid indexes", response = HttpStatus.class)
    })
    @ApiOperation(value = "Retrieve messages based on indexes", response = HttpStatus.class)
    public Callable<ResponseEntity<List<MessageRespone>>> getMessages(
            @PathVariable("userId") String userId,
            @PathVariable("startIndex") Integer startIndex ,
            @PathVariable("stopIndex") Integer stopIndex){
        return () -> {
            try {
                return new ResponseEntity<>(messagingService.getMessagesByIndex(userId, startIndex, stopIndex), HttpStatus.OK);
            } catch (MessageBoxMissingException me){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            } catch (MessageBoxIndexException me) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        };
    }

    @RequestMapping(
            value = "status/{userId}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Status retrieved", response = StatusResponse.class),
            @ApiResponse(code = 404, message = "No message box found for user", response = HttpStatus.class)
    })
    @ApiOperation(value = "Receives status of existing message box", response = HttpStatus.class)
    public Callable<ResponseEntity<StatusResponse>> getStatus(@PathVariable("userId") String userId){
        return () -> {
            try {
               return new ResponseEntity<>(messagingService.getStatus(userId), HttpStatus.OK);
            } catch (MessageBoxMissingException me){
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        };
    }

    @DeleteMapping
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successfully deleted messages"),
            @ApiResponse(code = 404, message = "No message box found for user"),
    })
    @ApiOperation(value = "Deletes messages from storage", response = HttpStatus.class)
    public Callable<ResponseEntity<HttpStatus>> deleteMessages(@RequestBody @Valid DeleteRequest deleteRequest){

        return () -> {
            messagingService.deleteMessages(deleteRequest.getUserId(), deleteRequest.getMessages());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        };
    }


}
