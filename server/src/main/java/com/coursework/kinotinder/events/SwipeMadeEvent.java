// SwipeMadeEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import org.springframework.context.ApplicationEvent;

public class SwipeMadeEvent extends ApplicationEvent {
    private final Room room;
    private final User user;
    private final Long movieId;
    private final String swipe;

    public SwipeMadeEvent(Room source, User user,Long movieId, String swipe) {
        super(source);
         this.room = source;
         this.user = user;
        this.movieId = movieId;
        this.swipe = swipe;
    }
       public Room getRoom() {
        return room;
    }
     public User getUser() {
        return user;
    }
    public Long getMovieId() {
        return movieId;
    }
     public String getSwipe() {
        return swipe;
    }
}