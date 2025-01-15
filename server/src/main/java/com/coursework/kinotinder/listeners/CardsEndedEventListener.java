// com/coursework/kinotinder/events/listeners/CardsEndedEventListener.java
package com.coursework.kinotinder.events.listeners;

import com.coursework.kinotinder.entities.Movie;
import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.events.CardsEndedEvent;
import com.coursework.kinotinder.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardsEndedEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final MovieRepository movieRepository;
      private static final Logger logger = LoggerFactory.getLogger(CardsEndedEventListener.class);

    @Async
    @EventListener
    public void handleCardsEndedEvent(CardsEndedEvent event) {
        Room room = event.getRoom();
         Long movieId = event.getMovieId();
        Movie movie = movieRepository.findById(movieId).get();
        messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/wait", movie);
        messagingTemplate.convertAndSend("/topic/rooms/" + room.getInviteCode() + "/events", "Cards ended, most liked movie " + movie.getTitle()  + " in room " + room.getInviteCode());
         logger.info("Cards ended, most liked movie {} in room {}", movie.getTitle(), room.getInviteCode());

    }
}