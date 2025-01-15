    package com.coursework.kinotinder.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Match {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long matchId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "movie_id")
    private Movie movie;

    private LocalDateTime matchedAt = LocalDateTime.now();
    
    private Integer totalLikes = 0;
    
    private Long sessionTime = 0L;
    
    private Integer membersCount;
    
    private Integer totalSwipes = 0;
}