// AchievementController.java
package com.coursework.kinotinder.controllers;

import com.coursework.kinotinder.entities.Achievement;
import com.coursework.kinotinder.entities.AchievementProgress;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.services.AchievementProgressService;
import com.coursework.kinotinder.services.AchievementService;
import com.coursework.kinotinder.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final AchievementProgressService progressService;
     private final UserService userService;

    // Получить все достижения
    @GetMapping
    public List<Achievement> getAllAchievements() {
        return achievementService.getAllAchievements();
    }

    // Получить прогресс пользователя
    @GetMapping("/{achievementId}/progress")
    public AchievementProgress getProgress(
            @PathVariable Long achievementId,
            @RequestParam String telegramId) {
       Achievement achievement = achievementService.getAchievementById(achievementId);
       User user = userService.getUserByTelegramId(telegramId);
        return progressService.getProgress(user, achievement)
                .orElseThrow(() -> new IllegalArgumentException("No progress found"));
    }

    // Обновить прогресс достижения
    @PostMapping("/{achievementId}/progress")
    public AchievementProgress updateProgress(
            @PathVariable Long achievementId,
            @RequestParam String telegramId,
            @RequestParam int progressIncrement) {
       Achievement achievement = achievementService.getAchievementById(achievementId);
        User user = userService.getUserByTelegramId(telegramId);
        return progressService.updateProgress(user, achievement, progressIncrement);
    }
}