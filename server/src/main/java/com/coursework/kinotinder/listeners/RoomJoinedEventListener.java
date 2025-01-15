 package com.coursework.kinotinder.events.listeners;
    
    import com.coursework.kinotinder.entities.Room;
    import com.coursework.kinotinder.entities.User;
    import com.coursework.kinotinder.events.RoomJoinedEvent;
    import com.coursework.kinotinder.services.RoomService;
    import lombok.RequiredArgsConstructor;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.context.event.EventListener;
    import org.springframework.messaging.simp.SimpMessagingTemplate;
    import org.springframework.scheduling.annotation.Async;
    import org.springframework.stereotype.Component;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import com.fasterxml.jackson.core.JsonProcessingException;
    import java.util.Map;
    
    @Component
    @RequiredArgsConstructor
    public class RoomJoinedEventListener {
    
        private final SimpMessagingTemplate messagingTemplate;
        private static final Logger logger = LoggerFactory.getLogger(RoomJoinedEventListener.class);
        private final ObjectMapper objectMapper;
        private final RoomService roomService;
    
        @Async
        @EventListener
        public void handleRoomJoinedEvent(RoomJoinedEvent event) {
            Room room = event.getRoom();
            User user = event.getUser();
             logger.info("[RoomJoinedEventListener] User {} joined room: {}", user.getTelegramId(), room.getInviteCode());
            try {
              messagingTemplate.convertAndSend("/topic/connect", objectMapper.writeValueAsString(Map.of("inviteCode",  room.getInviteCode())));
            
                 messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/ready", objectMapper.writeValueAsString(Map.of(
                         "readyCount", roomService.getRoomReadyUsersCount(room.getInviteCode()),
                         "participantsCount", roomService.getParticipantsCount(room.getInviteCode())
                 )));
                 logger.info("[RoomJoinedEventListener] Sent ready message to topic /topic/rooms/{}/ready:",room.getInviteCode());
            } catch (Exception e){
                logger.error("Error converting invite code to JSON", e);
            }
        }
    }