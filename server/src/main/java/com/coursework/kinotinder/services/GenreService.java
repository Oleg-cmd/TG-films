// GenreService.java
package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.Genre;
import com.coursework.kinotinder.repositories.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;

    public List<Genre> getAllGenres() {
        return genreRepository.findAll();
    }

    public List<Genre> getGenresByName(List<String> names){
        return genreRepository.findByNameIn(names);
    }
}