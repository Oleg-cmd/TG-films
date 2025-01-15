// UserRegisteredEvent.java
package com.coursework.kinotinder.events;

import com.coursework.kinotinder.entities.User;
import org.springframework.context.ApplicationEvent;

public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;
    public UserRegisteredEvent(User source) {
        super(source);
         this.user = source;
    }

     public User getUser() {
        return user;
    }
}