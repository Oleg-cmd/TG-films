package com.coursework.kinotinder.filters;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MovieFilter {

    private List<String> genres;
    private Double minRating;
    private Integer minYear;
    private Integer endYear;

}