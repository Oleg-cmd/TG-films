package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import org.springframework.context.ApplicationEvent;

public class MatchFoundEvent extends ApplicationEvent {
    private final Room room;
    private final Long movieId;

    public MatchFoundEvent(Room source,  Long movieId) {
        super(source);
          this.room = source;
        this.movieId = movieId;
    }
      public Room getRoom() {
        return room;
    }
    public Long getMovieId() {
        return movieId;
    }
}