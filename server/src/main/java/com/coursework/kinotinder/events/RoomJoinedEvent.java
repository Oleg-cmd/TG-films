// RoomJoinedEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import org.springframework.context.ApplicationEvent;

public class RoomJoinedEvent extends ApplicationEvent {

    private final Room room;
     private final User user;
    public RoomJoinedEvent(Room source, User user) {
        super(source);
        this.room = source;
         this.user = user;
    }

    public Room getRoom() {
        return room;
    }
    public User getUser() {
        return user;
    }
}