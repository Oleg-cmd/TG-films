package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genreId IN :genreIds AND m.externalRating >= :minRating AND m.releaseYear >= :minYear")
    List<Movie> findMoviesByFilter(@Param("genreIds") List<Long> genreIds,
                                    @Param("minRating") Double minRating,
                                    @Param("minYear") Integer minYear);
     @Query("SELECT m FROM Movie m JOIN m.genres g WHERE g.genreId IN :genreIds AND m.externalRating >= :minRating AND m.releaseYear >= :minYear AND m.releaseYear <= :endYear")
     List<Movie> findMoviesByFilter(@Param("genreIds") List<Long> genreIds,
                                     @Param("minRating") Double minRating,
                                     @Param("minYear") Integer minYear,
                                     @Param("endYear") Integer endYear);
    Optional<Movie> findByTitle(String title);
}