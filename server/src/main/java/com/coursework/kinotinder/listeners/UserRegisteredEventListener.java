// UserRegisteredEventListener.java
package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class UserRegisteredEventListener {

    private final SimpMessagingTemplate messagingTemplate;
       private static final Logger logger = LoggerFactory.getLogger(UserRegisteredEventListener.class);
    @Async
    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        User user = event.getUser();
      //  messagingTemplate.convertAndSend("/topic/users/" + user.getTelegramId() + "/events", "User " + user.getTelegramId() + " registered");
         logger.info("User {} registered", user.getTelegramId());
    }
}