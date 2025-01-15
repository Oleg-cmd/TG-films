// RoomCreatedEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import org.springframework.context.ApplicationEvent;

public class RoomCreatedEvent extends ApplicationEvent {

    private final Room room;
    public RoomCreatedEvent(Room source) {
        super(source);
        this.room = source;
    }

     public Room getRoom() {
        return room;
    }
}