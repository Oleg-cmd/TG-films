package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    List<Genre> findByNameIn(List<String> names);
    Optional<Genre> findByName(String name);
}
