package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.Achievement;
import com.coursework.kinotinder.entities.AchievementProgress;
import com.coursework.kinotinder.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AchievementProgressRepository extends JpaRepository<AchievementProgress, Long> {
    Optional<AchievementProgress> findByUserAndAchievement(User user, Achievement achievement);

    List<AchievementProgress> findAllByUser(User user);

    List<AchievementProgress> findAllByAchievement(Achievement achievement);

    List<AchievementProgress> findByUserAndAchieved(User user, Boolean achieved);
}