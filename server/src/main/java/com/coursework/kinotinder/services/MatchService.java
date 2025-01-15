package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.Match;
import com.coursework.kinotinder.entities.MatchRating;
import com.coursework.kinotinder.entities.Movie;
import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.CardsEndedEvent;
import com.coursework.kinotinder.events.MatchFoundEvent;
import com.coursework.kinotinder.events.SwipeMadeEvent;
import com.coursework.kinotinder.repositories.MatchRatingRepository;
import com.coursework.kinotinder.repositories.MatchRepository;
import com.coursework.kinotinder.repositories.MovieRepository;
import com.coursework.kinotinder.repositories.RoomRepository;
import com.coursework.kinotinder.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchRatingRepository matchRatingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private static final Logger logger = LoggerFactory.getLogger(MatchService.class);
    private final Map<Long, Map<String, Map<Long, String>>> roomSwipes = new HashMap<>();
    private final RoomService roomService;
    private final MovieRepository movieRepository;
    private final MovieService movieService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    public MatchService(MatchRepository matchRepository, MatchRatingRepository matchRatingRepository, RoomRepository roomRepository, UserRepository userRepository, ApplicationEventPublisher eventPublisher, RoomService roomService, MovieRepository movieRepository, MovieService movieService, SimpMessagingTemplate messagingTemplate, ObjectMapper objectMapper) {
        this.matchRepository = matchRepository;
        this.matchRatingRepository = matchRatingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.roomService = roomService;
        this.movieRepository = movieRepository;
         this.movieService = movieService;
         this.messagingTemplate = messagingTemplate;
        this.objectMapper = objectMapper;
    }

     public void processSwipe(String inviteCode, String telegramId, Long movieId, String swipe) {
      Room room = roomRepository.findByInviteCode(inviteCode)
              .orElseThrow(() -> new IllegalArgumentException("Room not found"));
      Long roomId = room.getRoomId();
      if (!roomSwipes.containsKey(roomId))
      {
       roomSwipes.put(roomId, new HashMap<>());
      }
      // Создание структур для хранения данных
       roomSwipes.computeIfAbsent(roomId, k -> new HashMap<>())
               .computeIfAbsent(telegramId, k -> new HashMap<>())
               .put(movieId, swipe);
       logger.info("User {} swiped {} for movie {} in room {}", telegramId, swipe, movieId, inviteCode);
       eventPublisher.publishEvent(new SwipeMadeEvent(room, userRepository.findByTelegramId(telegramId).get(), movieId, swipe));

       if (checkMatch(room, movieId)) {
           createMatch(room, movieId);
           eventPublisher.publishEvent(new MatchFoundEvent(room, movieId));
           logger.info("Match found for movie {} in room {}", movieId, inviteCode);
       } else if (isAllMoviesSwiped(room)) {
           Movie mostLikedMovie = getMostLikedMovie(room);
           eventPublisher.publishEvent(new CardsEndedEvent(room, mostLikedMovie != null ? mostLikedMovie.getMovieId() : null));
           logger.info("All movies swiped in room {}, most liked movie: {}", inviteCode, mostLikedMovie != null ? mostLikedMovie.getTitle() : "none");
           roomService.finishRoom(inviteCode);
       }
}
    public void clearSwipes(Room room) {
        roomSwipes.remove(room.getRoomId());
    }

  private boolean isAllMoviesSwiped(Room room) {
        if (!roomSwipes.containsKey(room.getRoomId())) {
            return false;
        }

        List<Movie> movies = movieService.assignMoviesToRoom(room, roomService.getRoomSettings(room.getInviteCode()), roomService);
        long totalMovies = movies.size();

        long totalSwiped = roomSwipes.get(room.getRoomId()).values().stream()
                .mapToLong(Map::size)
                .sum();
        return totalSwiped == totalMovies;
    }

    private Movie getMostLikedMovie(Room room) {
        Map<String, Map<Long, String>> swipes = roomSwipes.get(room.getRoomId());
        if (swipes == null || swipes.isEmpty()) {
            return null;
        }
        Map<Long, Integer> movieLikes = new HashMap<>();

        for (Map<Long, String> movieSwipes : swipes.values()) {
            for (Map.Entry<Long, String> movieEntry : movieSwipes.entrySet()) {
                Long movieId = movieEntry.getKey();
                String swipe = movieEntry.getValue();
                if ("like".equals(swipe)) {
                    movieLikes.put(movieId, movieLikes.getOrDefault(movieId, 0) + 1);
                }
            }
        }

        if (movieLikes.isEmpty()) {
            return null;
        }

        Long mostLikedMovieId = movieLikes.entrySet().stream()
                .max(Comparator.comparingInt(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElse(null);
        return mostLikedMovieId != null ? movieRepository.findById(mostLikedMovieId).orElse(null) : null;
    }

   private boolean checkMatch(Room room, Long movieId) {
        long participantCount = roomService.getParticipantsCount(room.getInviteCode());
        long selectionsCount = roomSwipes.get(room.getRoomId()).values().stream()
                .filter(map -> map.containsKey(movieId) && "like".equals(map.get(movieId)))
                .count();
        logger.info("Checking for match in room {}, participants: {}, likes: {}, movieId: {}", room.getInviteCode(), participantCount, selectionsCount, movieId);
        if (participantCount == 0) {
            return false;
        }
        return selectionsCount == participantCount;
    }

    private void createMatch(Room room, Long movieId) {
            Match match = new Match();
            Movie movie = new Movie();
            movie.setMovieId(movieId);
            match.setRoom(room);
            match.setMovie(movie);
            int likes =  (int) roomSwipes.get(room.getRoomId()).values().stream()
                    .filter(map -> map.containsKey(movieId) && "like".equals(map.get(movieId)))
                    .count();
            match.setTotalLikes(likes);
            int totalSwipes = (int) roomSwipes.get(room.getRoomId()).values().stream()
            .filter(map -> map.containsKey(movieId))
            .count();
            match.setTotalSwipes(totalSwipes);
            long participantCount = roomService.getParticipantsCount(room.getInviteCode());
            match.setMembersCount((int) participantCount);
            LocalDateTime roomCreatedAt = room.getCreatedAt();
            LocalDateTime now = LocalDateTime.now();
        long sessionTime =   Duration.between(roomCreatedAt, now).getSeconds();
            match.setSessionTime(sessionTime);
            matchRepository.save(match);
        }
 
    public List<Match> getAllMatches(User user) {
        return matchRepository.findByRoomCreator(user);
    }

        public MatchRating rateMatch(Long matchId, User user, Integer stars) {
          Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("Match not found"));
        MatchRating matchRating = matchRatingRepository.findByMatchAndUser(match, user)
            .orElseGet(() -> {
                  MatchRating newMatchRating = new MatchRating();
                newMatchRating.setMatch(match);
                newMatchRating.setUser(user);
              return newMatchRating;
            });
        matchRating.setStars(stars);
       matchRatingRepository.save(matchRating);
       try {
          messagingTemplate.convertAndSend("/topic/rooms/" + match.getRoom().getInviteCode() + "/rating",
                objectMapper.writeValueAsString(Map.of(
                         "matchId", matchId,
                        "telegramId", user.getTelegramId(),
                        "stars", stars
                  )));
           logger.info("Sent match rating  for match {} to room {}", matchId, match.getRoom().getInviteCode());
        } catch (JsonProcessingException e) {
            logger.error("Error converting rating to JSON", e);
        }
        return matchRating;
    }


        public  MatchRating getMatchRatingForUser(Long matchId, User user){
              Match match = matchRepository.findById(matchId)
                      .orElseThrow(() -> new IllegalArgumentException("Match not found"));
           return matchRatingRepository.findByMatchAndUser(match, user).orElse(null);
    }
}