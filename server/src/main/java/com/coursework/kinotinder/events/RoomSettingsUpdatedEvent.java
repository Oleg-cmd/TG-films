// RoomSettingsUpdatedEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.Room;
import org.springframework.context.ApplicationEvent;
import java.util.Map;

public class RoomSettingsUpdatedEvent extends ApplicationEvent {

   private final Room room;
   private final Map<String, Object> settings;
    public RoomSettingsUpdatedEvent(Room source, Map<String, Object> settings) {
        super(source);
        this.room = source;
        this.settings = settings;
    }
       public Room getRoom() {
        return room;
    }
     public Map<String, Object> getSettings() {
        return settings;
    }
}