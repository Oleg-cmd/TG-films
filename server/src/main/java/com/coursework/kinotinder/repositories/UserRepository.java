// UserRepository.java
package com.coursework.kinotinder.repositories;

import com.coursework.kinotinder.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByTelegramId(String telegramId);
}