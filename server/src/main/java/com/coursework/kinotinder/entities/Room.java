package com.coursework.kinotinder.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private String name;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private User creator;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime finishedAt;

     @Column(unique = true)
    private String inviteCode;

    // Геттеры и сеттеры, конструкторы (с Lombok)
}