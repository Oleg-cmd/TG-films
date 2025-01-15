package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.Match;
import com.coursework.kinotinder.entities.MatchRating;
import com.coursework.kinotinder.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRatingRepository extends JpaRepository<MatchRating, Long> {

    List<MatchRating> findByMatch(Match match);
    Optional<MatchRating> findByMatchAndUser(Match match, User user);
}