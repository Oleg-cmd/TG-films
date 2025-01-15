// UserService.java
package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.UserRegisteredEvent;
import com.coursework.kinotinder.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public User registerUser(User user) {
        User registeredUser = userRepository.save(user);
        logger.info("User registered: {}", registeredUser.getTelegramId());
        eventPublisher.publishEvent(new UserRegisteredEvent(registeredUser));
        return registeredUser;
    }

    public User getUserByTelegramId(String telegramId) {
        logger.info("Get user by telegramId: {}", telegramId);
        return userRepository.findByTelegramId(telegramId).orElse(null);
    }
}