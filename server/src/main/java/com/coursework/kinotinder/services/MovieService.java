package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.Genre;
import com.coursework.kinotinder.entities.Movie;
import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.filters.MovieFilter;
import com.coursework.kinotinder.repositories.GenreRepository;
import com.coursework.kinotinder.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Date;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private static final Logger logger = LoggerFactory.getLogger(MovieService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

     public List<Movie> getFilteredMovies(MovieFilter filter, List<String> genres) {
        List<Long> genreIds = genres.stream()
                .map(genreName -> genreRepository.findByName(genreName)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + genreName))
                .getGenreId())
                .collect(Collectors.toList());

         Double minRating = filter.getMinRating() != null ? filter.getMinRating() : 1.0;
        Integer minYear = filter.getMinYear() != null ? filter.getMinYear() : 1900;
        Integer endYear = filter.getEndYear() != null ? filter.getEndYear() : new Date().getYear();

        if (genreIds.isEmpty()) {
            genreIds = genreRepository.findAll().stream()
                .map(Genre::getGenreId)
                .toList();
        }
           List<Movie> movies =  movieRepository.findMoviesByFilter(genreIds, minRating, minYear, endYear);
        logger.info("Filtered movies: {}, genres: {}, minRating: {}, minYear: {}, endYear: {}", movies.size(), genreIds, minRating, minYear, endYear);
        return movies;
    }

    public List<Movie> assignMoviesToRoom(Room room, Map<String, Object> settings, RoomService roomService) {
        MovieFilter filter = new MovieFilter();
        
        // Корректное приведение типов
        filter.setMinYear((Integer) settings.get("startYear"));
        filter.setEndYear((Integer) settings.get("endYear"));
        filter.setMinRating(settings.get("rating") instanceof Integer 
            ? ((Integer) settings.get("rating")).doubleValue() 
            : (Double) settings.get("rating"));
        
        List<Movie> movies = getFilteredMovies(filter, (List<String>) settings.get("selectedGenres"));

        logger.info("Movies assigned to room {}: {}", room.getInviteCode(), movies.size());
        return movies;
    }
}