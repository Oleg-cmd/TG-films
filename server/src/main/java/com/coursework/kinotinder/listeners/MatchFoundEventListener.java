  package com.coursework.kinotinder.events.listeners;

  import com.coursework.kinotinder.entities.Genre;
  import com.coursework.kinotinder.entities.Movie;
  import com.coursework.kinotinder.entities.Room;
  import com.coursework.kinotinder.events.MatchFoundEvent;
  import com.coursework.kinotinder.repositories.MovieRepository;
  import lombok.RequiredArgsConstructor;
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.context.event.EventListener;
  import org.springframework.messaging.simp.SimpMessagingTemplate;
  import org.springframework.scheduling.annotation.Async;
  import org.springframework.stereotype.Component;
  import com.fasterxml.jackson.databind.ObjectMapper;
  import com.fasterxml.jackson.core.JsonProcessingException;
  import org.springframework.transaction.annotation.Transactional;
  import java.util.List;
  import java.util.stream.Collectors;
  import java.util.Map;

  @Component
  @RequiredArgsConstructor
  public class MatchFoundEventListener {
      private final SimpMessagingTemplate messagingTemplate;
      private final MovieRepository movieRepository;
       private final ObjectMapper objectMapper;
      private static final Logger logger = LoggerFactory.getLogger(MatchFoundEventListener.class);
      @Async
      @EventListener
        @Transactional
      public void handleMatchFoundEvent(MatchFoundEvent event) {
          Room room = event.getRoom();
          Long movieId = event.getMovieId();
          Movie movie = movieRepository.findById(movieId).get();
            try {
             List<String> genres =  movie.getGenres().stream().map(Genre::getName).collect(Collectors.toList());
                messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/match", objectMapper.writeValueAsString(Map.of(
                    "movieId", movie.getMovieId(),
                      "title", movie.getTitle(),
                      "releaseYear", movie.getReleaseYear(),
                      "externalRating", movie.getExternalRating(),
                      "posterUrl", movie.getPosterUrl(),
                      "description", movie.getDescription(),
                      "genres", genres
                )));
               messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", "Match found for movie " + movie.getTitle()  + " in room " + room.getInviteCode());
               logger.info("Match found for movie {} in room {}", movie.getTitle(), room.getInviteCode());
            } catch (JsonProcessingException e) {
                logger.error("Error converting match to JSON", e);
            }

      }
  }