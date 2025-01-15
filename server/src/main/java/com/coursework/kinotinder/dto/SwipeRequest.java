package com.coursework.kinotinder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SwipeRequest {

    private String telegramId;
    private Long movieId;
    private String swipe;
}