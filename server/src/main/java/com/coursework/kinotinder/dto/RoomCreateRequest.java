// RoomCreateRequest.java
package com.coursework.kinotinder.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RoomCreateRequest {
  private String telegramId;
  private String roomName;
}