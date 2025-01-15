package com.coursework.kinotinder.controllers;

import com.coursework.kinotinder.dto.SwipeRequest;
import com.coursework.kinotinder.services.MatchService;
import com.coursework.kinotinder.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.io.IOException;

@Controller
@RequiredArgsConstructor
public class RoomSocketController {

    private final RoomService roomService;
    private final MatchService matchService;
    private static final Logger logger = LoggerFactory.getLogger(RoomSocketController.class);
    private final ObjectMapper objectMapper;

     @MessageMapping("/connect")
    public Map<String, String> connect(@Payload String payload) {
        logger.info("Received connect request: {}", payload);
        return roomService.createRoom(payload, null);
    }
     @MessageMapping("/rooms/{inviteCode}/join")
    public void join(@Payload String payload, @DestinationVariable String inviteCode) {
        logger.info("Received join request for room {}: {}", inviteCode, payload);
        roomService.joinRoom(inviteCode, payload);
    }


    @MessageMapping("/rooms/{inviteCode}/swipe")
      public void processSwipe(@DestinationVariable String inviteCode, @Payload SwipeRequest swipeRequest)  {
        logger.info("Received swipe request: {}, inviteCode {}", swipeRequest, inviteCode);
        matchService.processSwipe(inviteCode, swipeRequest.getTelegramId(), swipeRequest.getMovieId(), swipeRequest.getSwipe());
    }

    @MessageMapping("/rooms/{inviteCode}/ready")
     public Map<String, Object> ready(@DestinationVariable String inviteCode, @Payload Map<String, Object> payload) {
        logger.info("Received ready request: {}, inviteCode {}", payload, inviteCode);
        return  roomService.setReady(inviteCode, (String) payload.get("telegramId"), (Boolean) payload.get("isReady"));
    }

    @MessageMapping("/rooms/{inviteCode}/settings")
     public Map<String,Object> settings(@DestinationVariable String inviteCode, @Payload Map<String,Object> payload)  {
           logger.info("Received settings request: {}, inviteCode {}", payload, inviteCode);
        return  roomService.updateRoomSettings(inviteCode, payload);
    }

    @MessageMapping("/rooms/{inviteCode}/disconnect")
    public void disconnect(@DestinationVariable String inviteCode, @Payload Map<String, Object> payload) {
      logger.info("Received disconnect request: {}, inviteCode: {}", payload, inviteCode);
         String telegramId = (String) payload.get("telegramId");
         roomService.disconnectUser(inviteCode, telegramId);
    }

    @MessageExceptionHandler
    public String handleException(Throwable exception){
        logger.error("Error in websocket",exception);
        return exception.getMessage();
    }

    
}