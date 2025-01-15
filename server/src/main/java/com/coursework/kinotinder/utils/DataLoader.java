package com.coursework.kinotinder.utils;

import com.coursework.kinotinder.entities.Genre;
import com.coursework.kinotinder.entities.Movie;
import com.coursework.kinotinder.repositories.GenreRepository;
import com.coursework.kinotinder.repositories.MovieRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataLoader {
    private final GenreRepository genreRepository;
    private final MovieRepository movieRepository;
    private final ObjectMapper objectMapper;
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private static final int MAX_LENGTH = 255;
    private static final String DEFAULT_GENRE = "Action";


    @PostConstruct
    public void loadData() {
        logger.info("Start seeding data from JSON");
        try {
            // Load Genres
            List<Genre> genres = loadGenresFromJson("genres.json");
            Map<String, Genre> genreMap = new HashMap<>();
            for (Genre genre : genres) {
                genreMap.put(genre.getName(), genre);
            }
            logger.info("Loaded {} genres", genres.size());
            genreRepository.saveAll(genres);
            logger.info("Saved {} genres", genres.size());


            // Load Movies
            List<Movie> movies = loadMoviesFromJson("movies.json", genreMap);
            logger.info("Loaded {} movies", movies.size());
            movieRepository.saveAll(movies);
            logger.info("Saved {} movies", movies.size());
            logger.info("Seeding data from JSON successfully");
        } catch (IOException e) {
            logger.error("Error loading data from JSON: {}", e.getMessage());
        }

    }

    private List<Genre> loadGenresFromJson(String filename) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IOException("File not found " + filename);
        }
        logger.info("Start reading genres from JSON");
        TypeReference<List<Map<String, String>>> typeReference = new TypeReference<>() {
        };
        List<Map<String, String>> genresData = objectMapper.readValue(inputStream, typeReference);
        List<Genre> genres = new ArrayList<>();
        for (Map<String, String> genreData : genresData) {
            String genreName = genreData.get("name");
            if (genreName != null) {
                if (genreRepository.findByName(genreName).isEmpty()) {
                    Genre genre = new Genre();
                    genre.setName(genreName);
                    genres.add(genre);
                } else {
                    logger.warn("Genre {} already exists, skipping...", genreName);
                }
            } else {
                logger.warn("Genre name is null, skipping");
            }
        }
        logger.info("Loaded {} genres from JSON", genres.size());
        return genres;
    }

    private List<Movie> loadMoviesFromJson(String filename, Map<String, Genre> genreMap) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            throw new IOException("File not found: " + filename);
        }
        logger.info("Start reading movies from JSON");

        TypeReference<List<Map<String, Object>>> typeReference = new TypeReference<>() {
        };
        List<Map<String, Object>> moviesData = objectMapper.readValue(inputStream, typeReference);
        List<Movie> movies = new ArrayList<>();
        for (Map<String, Object> movieData : moviesData) {
            try {
                String movieTitle = (String) movieData.get("title");
                if (movieTitle != null) {
                    if (movieRepository.findByTitle(movieTitle).isEmpty()) {
                        Movie movie = new Movie();
                        movie.setTitle(truncateString(movieTitle, MAX_LENGTH));
                        String description = (String) movieData.get("description");
                        if (description != null) {
                            movie.setDescription(truncateString(description, MAX_LENGTH));
                        }

                        if (movieData.get("release_year") != null) {
                            movie.setReleaseYear((Integer) movieData.get("release_year"));
                        }
                        if (movieData.get("external_rating") != null) {
                            movie.setExternalRating(Double.parseDouble(movieData.get("external_rating").toString()));
                        }
                        movie.setPosterUrl((String) movieData.get("poster_url"));
                        List<String> movieGenres = (List<String>) movieData.get("genres");
                        List<Genre> genresForMovie = new ArrayList<>();
                        if (movieGenres != null) {
                            for (String genreName : movieGenres) {
                                Genre genre = genreMap.get(genreName);
                                if (genre != null) {
                                    genresForMovie.add(genre);
                                } else {
                                    // Если жанр не найден, создаем его и добавляем
                                    Optional<Genre> existingGenre = genreRepository.findByName(genreName);
                                    if(existingGenre.isPresent()){
                                        genresForMovie.add(existingGenre.get());
                                         logger.warn("Genre {} already exists, for movie {}, skipping adding", genreName, movieTitle);
                                    } else {
                                        Genre newGenre = new Genre();
                                        newGenre.setName(genreName);
                                          genreRepository.save(newGenre);
                                        genresForMovie.add(newGenre);
                                          genreMap.put(genreName, newGenre); // Добавляем в мапу, чтобы не создавать заново
                                        logger.warn("Genre {} not found for movie {}, creating...", genreName, movieTitle);
                                    }
                                }
                            }
                        }
                        if (!genresForMovie.isEmpty()) {
                            movie.setGenres(genresForMovie);
                        } else {
                            logger.warn("No genres found for movie {}, setting default genre", movieTitle);
                            Optional<Genre> defaultGenre = genreRepository.findByName(DEFAULT_GENRE);
                            if (defaultGenre.isPresent()) {
                                movie.setGenres(List.of(defaultGenre.get()));
                            } else {
                                logger.warn("Default genre not found, skipping movie {}", movieTitle);
                                continue;
                            }

                        }

                        movies.add(movie);
                    } else {
                        logger.warn("Movie {} already exists, skipping...", movieTitle);
                    }
                } else {
                    logger.warn("Movie title is null, skipping");
                }

            } catch (Exception e) {
                logger.warn("Error loading movie : {}, Skipping...", movieData, e);
            }
        }
        logger.info("Loaded {} movies from JSON", movies.size());
        return movies;
    }

    private String truncateString(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }
}