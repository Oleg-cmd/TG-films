// com/coursework/kinotinder/events/CardsEndedEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class CardsEndedEvent extends ApplicationEvent {
    private final Room room;
    private final Long movieId;

    public CardsEndedEvent(Room room, Long movieId) {
        super(room);
        this.room = room;
        this.movieId = movieId;
    }
}