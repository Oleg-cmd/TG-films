// AchievementProgress.java
package com.coursework.kinotinder.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class AchievementProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "achievement_id", referencedColumnName = "achievementId", nullable = false)
    private Achievement achievement;

    @Column(nullable = false)
    private Integer progress = 0;

    @Column(nullable = false)
    private Boolean achieved = false;
}