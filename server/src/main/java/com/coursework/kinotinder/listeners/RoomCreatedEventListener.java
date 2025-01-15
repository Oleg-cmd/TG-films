// RoomCreatedEventListener.java
package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.events.RoomCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomCreatedEventListener {
    private static final Logger logger = LoggerFactory.getLogger(RoomCreatedEventListener.class);

    @Async
    @EventListener
    public void handleRoomCreatedEvent(RoomCreatedEvent event) {
        Room room = event.getRoom();
       logger.info("Room {} created. Event log", room.getInviteCode());
    }
}