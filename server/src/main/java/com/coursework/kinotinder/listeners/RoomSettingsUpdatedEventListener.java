package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.events.RoomSettingsUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import com.fasterxml.jackson.core.JsonProcessingException;

@Component
@RequiredArgsConstructor
public class RoomSettingsUpdatedEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(RoomSettingsUpdatedEventListener.class);
    private final ObjectMapper objectMapper;

    @Async
    @EventListener
    public void handleRoomSettingsUpdatedEvent(RoomSettingsUpdatedEvent event) {
        Room room = event.getRoom();
        Map<String, Object> settings = event.getSettings();
        String inviteCode = room.getInviteCode();
        logger.info("[RoomSettingsUpdatedEventListener] Received settings update event for room: {}", inviteCode);
        try {
            String settingsJson = objectMapper.writeValueAsString(settings);
            logger.info("[RoomSettingsUpdatedEventListener] Sending settings to topic /topic/rooms/{}/settings: {}", inviteCode, settingsJson);
            messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/settings", settingsJson);
        } catch (JsonProcessingException e) {
            logger.error("[RoomSettingsUpdatedEventListener] Error converting settings to JSON", e);
        }
        logger.info("[RoomSettingsUpdatedEventListener] Sending event message to topic /topic/rooms/{}/events: Settings updated for room {}", inviteCode, inviteCode);
        messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/events", "Settings updated for room " + inviteCode);


    }
}