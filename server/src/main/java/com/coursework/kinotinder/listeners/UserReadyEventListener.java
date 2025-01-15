package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.Genre;
import com.coursework.kinotinder.entities.Movie;
import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.UserReadyEvent;
import com.coursework.kinotinder.services.MovieService;
import com.coursework.kinotinder.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.HashMap;
@Component
@RequiredArgsConstructor
public class UserReadyEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final MovieService movieService;
    private final ObjectMapper objectMapper;
    private final RoomService roomService;
    private static final Logger logger = LoggerFactory.getLogger(UserReadyEventListener.class);
    private static final int BATCH_SIZE = 20;
    private final Map<String, List<Map<String, Object>>> movieBatches = new HashMap<>();
    @Async
    @EventListener
    public void handleUserReadyEvent(UserReadyEvent event) {
        Room room = event.getRoom();
        User user = event.getUser();
        try {
            messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/ready", objectMapper.writeValueAsString(Map.of(
                    "readyCount", roomService.getRoomReadyUsersCount(room.getInviteCode()),
                       "participantsCount", roomService.getParticipantsCount(room.getInviteCode())
            )));
        } catch (JsonProcessingException e) {
              logger.error("Error converting ready count to JSON", e);
        }
         messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", "User " + user.getTelegramId() + " is ready in room " + room.getInviteCode());
         logger.info("User {} is ready in room {}", user.getTelegramId(), room.getInviteCode());
         if(roomService.getParticipantsCount(room.getInviteCode()) == roomService.getRoomReadyUsersCount(room.getInviteCode())){
            List<Movie> movies = movieService.assignMoviesToRoom(room, roomService.getRoomSettings(room.getInviteCode()), roomService);
             if(movies.isEmpty()){
                  try {
                      messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", objectMapper.writeValueAsString(Map.of(
                              "message", "No movies found for selected filters"
                      )));
                      logger.info("No movies found for selected filters in room {}", room.getInviteCode());
                  } catch (JsonProcessingException e) {
                      logger.error("Error converting error to JSON", e);
                  }
              } else {
                  try {
                    messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", objectMapper.writeValueAsString(Map.of(
                            "message", "Game started in room " + room.getInviteCode()
                    )));
                      List<Map<String,Object>> responseMovies =  movies.stream().map(movie -> {
                          List<String> genres =   movie.getGenres().stream().map(Genre::getName).collect(Collectors.toList());
                          return Map.of(
                                  "movieId", movie.getMovieId(),
                                  "title", movie.getTitle(),
                                  "releaseYear", movie.getReleaseYear(),
                                  "externalRating", movie.getExternalRating(),
                                  "posterUrl", movie.getPosterUrl(),
                                  "description", movie.getDescription(),
                                  "genres", genres
                          );
                      }).collect(Collectors.toList());

                     movieBatches.put(room.getInviteCode(), new ArrayList<>());
                        for (int i = 0; i < responseMovies.size(); i += BATCH_SIZE) {
                            int end = Math.min(i + BATCH_SIZE, responseMovies.size());
                             List<Map<String,Object>> batch = responseMovies.subList(i, end);
                            movieBatches.get(room.getInviteCode()).add(Map.of("movies", batch));
                        }
                       sendNextBatch(room.getInviteCode());
                  logger.info("Game started in room {}, sending movies", room.getInviteCode());
                } catch (JsonProcessingException e) {
                    logger.error("Error converting movie to JSON", e);
                }
            }
        }

    }
    public void sendNextBatch(String inviteCode) throws JsonProcessingException {
            if (movieBatches.containsKey(inviteCode)){
               List<Map<String, Object>> batches = movieBatches.get(inviteCode);
               if(!batches.isEmpty()){
                   messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/movies", objectMapper.writeValueAsString(batches.remove(0)));
                    logger.info("Sending  movies batch to room {}",  inviteCode);
               } else {
                   movieBatches.remove(inviteCode);
                   logger.info("All batches were sent for room {}", inviteCode);
               }
             }

    }
}