// UserController.java
package com.coursework.kinotinder.controllers;

import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Получение пользователя по telegramId
    @GetMapping("/{telegramId}")
    public User getUserByTelegramId(@PathVariable String telegramId) {
        return userService.getUserByTelegramId(telegramId);
    }
}