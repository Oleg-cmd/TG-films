// RoomController.java
package com.coursework.kinotinder.controllers;

import com.coursework.kinotinder.dto.RoomCreateRequest;
import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @PostMapping("/create")
    public Map<String, String> createRoom(@RequestBody RoomCreateRequest request) {
        return roomService.createRoom(request.getTelegramId(), request.getRoomName());
    }

    @GetMapping("/{inviteCode}")
    public Room getRoomByInviteCode(@PathVariable String inviteCode) {
        return roomService.getRoomByInviteCode(inviteCode);
    }

}