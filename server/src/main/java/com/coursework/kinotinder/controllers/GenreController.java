// GenreController.java
package com.coursework.kinotinder.controllers;

import com.coursework.kinotinder.entities.Genre;
import com.coursework.kinotinder.services.GenreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public List<Genre> getAllGenres(){
        return genreService.getAllGenres();
    }
}