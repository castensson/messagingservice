package se.castensson.messaging.controller;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import se.castensson.messaging.api.response.MessageRespone;
import se.castensson.messaging.api.response.StatusResponse;
import se.castensson.messaging.service.MessagingService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;



@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(properties = {"spring.main.allow-bean-definition-overriding=true"})
@AutoConfigureMockMvc
class MessagingControllerTest {

    @Autowired
    private MockMvc webTestClient;

    @MockBean
    private MessagingService messagingService;

    @Test
    void getUnread() throws Exception {

        when(messagingService.getUnreadMessages(any()))
                .thenReturn(List.of(new MessageRespone("TEST", UUID.randomUUID(), LocalDateTime.now(), "UNFETCHED")));

        MvcResult result1 = webTestClient.perform(
                get("/api/messaging/unread/TEST_USER")
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        webTestClient.perform(asyncDispatch(result1))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].text").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].received").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].messageId").exists())
                .andExpect(MockMvcResultMatchers.header().exists("Content-Type"));
    }

    @Test
    void getStatus() throws Exception {

        when(messagingService.getStatus(any()))
                .thenReturn(new StatusResponse(10L, 20L)).thenCallRealMethod();

        MvcResult result1 = webTestClient.perform(
                get("/api/messaging/status/TEST_USER")
                        .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        webTestClient.perform(asyncDispatch(result1))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.fetched").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.unfetched").exists())
                .andExpect(MockMvcResultMatchers.header().exists("Content-Type"));
    }
}

