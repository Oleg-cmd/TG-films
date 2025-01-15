// SwipeMadeEventListener.java
package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.SwipeMadeEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SwipeMadeEventListener {

    private final SimpMessagingTemplate messagingTemplate;
        private static final Logger logger = LoggerFactory.getLogger(SwipeMadeEventListener.class);


    @Async
    @EventListener
    public void handleSwipeMadeEvent(SwipeMadeEvent event) {
        Room room = event.getRoom();
        User user = event.getUser();
        Long movieId = event.getMovieId();
        String swipe = event.getSwipe();
         messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", "User " + user.getTelegramId() + " swipe " + swipe + " for movie " + movieId  + " in room " + room.getInviteCode());
        logger.info("User {} swipe {} for movie {} in room {}", user.getTelegramId(), swipe, movieId, room.getInviteCode());
    }
}
