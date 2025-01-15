package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.Achievement;
import com.coursework.kinotinder.entities.AchievementProgress;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.repositories.AchievementProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AchievementProgressService {

    private final AchievementProgressRepository achievementProgressRepository;

    public Optional<AchievementProgress> getProgress(User user, Achievement achievement) {
        return achievementProgressRepository.findByUserAndAchievement(user, achievement);
    }

    public AchievementProgress updateProgress(User user, Achievement achievement, int progressIncrement) {
        AchievementProgress progress = achievementProgressRepository
                .findByUserAndAchievement(user, achievement)
                .orElseGet(() -> {
                    AchievementProgress newProgress = new AchievementProgress();
                    newProgress.setUser(user);
                    newProgress.setAchievement(achievement);
                    return newProgress;
                });

        progress.setProgress(progress.getProgress() + progressIncrement);
        if (progress.getProgress() >= 100) { // Условие получения достижения
            progress.setAchieved(true);
        }

        return achievementProgressRepository.save(progress);
    }

    public List<AchievementProgress> getCompletedAchievements(User user) {
        return achievementProgressRepository.findByUserAndAchieved(user, true)
                .stream().toList();
    }
}