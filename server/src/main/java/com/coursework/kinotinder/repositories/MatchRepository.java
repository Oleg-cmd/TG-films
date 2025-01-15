package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.Match;
import com.coursework.kinotinder.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
      List<Match> findByRoomCreator(User user);
}