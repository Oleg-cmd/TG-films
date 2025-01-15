package com.coursework.kinotinder.services;

import com.coursework.kinotinder.entities.Room;
import com.coursework.kinotinder.entities.User;
import com.coursework.kinotinder.events.RoomCreatedEvent;
import com.coursework.kinotinder.events.RoomJoinedEvent;
import com.coursework.kinotinder.events.RoomSettingsUpdatedEvent;
import com.coursework.kinotinder.events.UserReadyEvent;
import com.coursework.kinotinder.repositories.RoomRepository;
import com.coursework.kinotinder.repositories.UserRepository;
import com.coursework.kinotinder.utils.RandomStringGenerator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RoomService {

    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final RandomStringGenerator randomStringGenerator;
    private final  Map<String, Map<String,Object>> roomsSettings = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    private final MovieService movieService;
    private static final int DEFAULT_START_YEAR = 1990;
    private static final double DEFAULT_RATING = 5.0;
    private static final List<String> DEFAULT_GENRES = List.of("Drama");
    private final  Map<String, Set<String>> roomReadyUsers = new HashMap<>();
    private final Map<String, List<User>> roomParticipants = new HashMap<>();

    public Map<String, String> createRoom(String telegramId, String roomName) {
        User creator = userRepository.findByTelegramId(telegramId)
                .orElse(null);
        if (creator == null){
            User user = new User();
            user.setTelegramId(telegramId);
            creator = userRepository.save(user);
        }
        Room room = new Room();
        room.setCreator(creator);
        room.setName(roomName);
        room.setInviteCode(randomStringGenerator.generate());
        room = roomRepository.save(room);
        Map<String,Object> defaultSettings =  getDefaultSettings();
        roomsSettings.put(room.getInviteCode(), defaultSettings);
        roomReadyUsers.put(room.getInviteCode(), new HashSet<>());
        roomParticipants.put(room.getInviteCode(), new ArrayList<>());
        logger.info("Room created: {}, creator: {}", room.getInviteCode(), creator.getTelegramId());
        try {
            messagingTemplate.convertAndSend("/topic/connect", objectMapper.writeValueAsString(Map.of("inviteCode",  room.getInviteCode())));
        } catch (JsonProcessingException e) {
            logger.error("Error converting invite code to JSON", e);
            throw new RuntimeException(e);
        }
        eventPublisher.publishEvent(new RoomCreatedEvent(room));
        joinRoom(room.getInviteCode(), telegramId);
           updateRoomSettings(room.getInviteCode(), defaultSettings);
        return Map.of("inviteCode", room.getInviteCode());
    }
    private Map<String, Object> getDefaultSettings() {
        Map<String,Object> defaultSettings = new HashMap<>();
        defaultSettings.put("selectedGenres", DEFAULT_GENRES);
        defaultSettings.put("startYear", DEFAULT_START_YEAR);
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        defaultSettings.put("endYear", currentYear);
        defaultSettings.put("rating", DEFAULT_RATING);
        return defaultSettings;
    }
    public Room getRoomByInviteCode(String inviteCode) {
        logger.info("Get room by invite code: {}", inviteCode);
        return roomRepository.findByInviteCode(inviteCode)
                .orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }
    public int getParticipantsCount(String inviteCode) {
        Room room = getRoomByInviteCode(inviteCode);
        return  roomParticipants.computeIfAbsent(inviteCode, k -> new ArrayList<>()).size();
    }
    public int getRoomReadyUsersCount(String inviteCode){
        return  roomReadyUsers.computeIfAbsent(inviteCode, k -> new HashSet<>()).size();
    }
    public  Map<String, Object> getRoomSettings(String inviteCode){
        return roomsSettings.get(inviteCode);
    }

    public Room joinRoom(String inviteCode, String telegramId) {
         logger.info("[joinRoom] Starting join room process, inviteCode: {}, telegramId: {}", inviteCode, telegramId);
        Room room = getRoomByInviteCode(inviteCode);
        User user = userRepository.findByTelegramId(telegramId)
                .orElse(null);
        if(user == null){
            User newUser = new User();
            newUser.setTelegramId(telegramId);
            user = userRepository.save(newUser);
        }

        // Проверяем, есть ли уже участник с таким telegramId в комнате
        List<User> participants = roomParticipants.computeIfAbsent(inviteCode, k -> new ArrayList<>());
        boolean alreadyJoined = participants.stream()
                .anyMatch(p -> p.getTelegramId().equals(telegramId));

        if (alreadyJoined) {
            // Обработка ситуации, когда пользователь уже присоединился
            logger.info("[joinRoom] User {} already joined room: {}", telegramId, inviteCode);
            Map<String,Object> settings =  roomsSettings.get(inviteCode);
            if (settings == null){
                settings = getDefaultSettings();
                 logger.info("[joinRoom] Settings are null, using default settings: {}", settings);
            }
            try {
                 logger.info("[joinRoom] Sending settings to topic /topic/rooms/{}/settings: {}", inviteCode, settings);
                if(settings != null){
                    messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/settings", objectMapper.writeValueAsString(settings));
                }
                messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/ready", objectMapper.writeValueAsString(Map.of(
                        "readyCount", getRoomReadyUsersCount(inviteCode),
                        "participantsCount", getParticipantsCount(inviteCode)
                )));
                logger.info("[joinRoom] Sent ready message");
            } catch (JsonProcessingException e) {
                logger.error("Error converting settings to JSON", e);
            }
            return room;
        }

        roomParticipants.computeIfAbsent(inviteCode, k-> new ArrayList<>()).add(user);

        logger.info("[joinRoom] User {} joined room: {}", telegramId, inviteCode);
         String participantsString = roomParticipants.get(inviteCode).stream()
                .map(p -> "TelegramId: " + p.getTelegramId())
                .collect(Collectors.joining(", "));
        logger.info("[joinRoom] Current participants in room {}: {}", inviteCode, participantsString);
        eventPublisher.publishEvent(new RoomJoinedEvent(room, user));
        try {
            Map<String,Object> settings =  roomsSettings.get(inviteCode);
            if (settings == null){
                   settings = getDefaultSettings();
                   roomsSettings.put(inviteCode, settings);
                   logger.info("[joinRoom] settings is null, setting default settings: {}", settings);
            }
                logger.info("[joinRoom] Sending settings to topic /topic/rooms/{}/settings: {}", inviteCode, settings);
            if (settings != null) {
              messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/settings", objectMapper.writeValueAsString(settings));
            }
            messagingTemplate.convertAndSend("/topic/rooms/" + inviteCode + "/ready", objectMapper.writeValueAsString(Map.of(
                    "readyCount", getRoomReadyUsersCount(inviteCode),
                    "participantsCount", getParticipantsCount(inviteCode)
            )));
               logger.info("[joinRoom] Sent ready message");
        } catch (JsonProcessingException e) {
            logger.error("Error converting settings to JSON", e);
        }
        return roomRepository.save(room);
    }

    public Map<String,Object> updateRoomSettings(String inviteCode, Map<String, Object> settings) {
        roomsSettings.put(inviteCode,settings);
        logger.info("Settings updated for room: {}, settings: {}", inviteCode, settings);
        eventPublisher.publishEvent(new RoomSettingsUpdatedEvent(getRoomByInviteCode(inviteCode),settings));
        return settings;
    }

    public Map<String, Object> setReady(String inviteCode, String telegramId, boolean isReady) {
        Room room = getRoomByInviteCode(inviteCode);
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if(isReady){
             roomReadyUsers.computeIfAbsent(inviteCode, k -> new HashSet<>()).add(user.getTelegramId());
              logger.info("User {} set ready in room: {}", telegramId, inviteCode);
        }else {
           roomReadyUsers.computeIfAbsent(inviteCode, k -> new HashSet<>()).remove(user.getTelegramId());
             logger.info("User {} set not ready in room: {}", telegramId, inviteCode);
        }
        eventPublisher.publishEvent(new UserReadyEvent(room,user));

        roomRepository.save(room);
          return Map.of("readyCount", roomReadyUsers.get(inviteCode).size(), "participantsCount", getParticipantsCount(inviteCode));
    }

    public void finishRoom(String inviteCode) {
        Room room = getRoomByInviteCode(inviteCode);
        room.setFinishedAt(LocalDateTime.now());
        logger.info("Room {} finished", inviteCode);
        roomRepository.save(room);
    }

     public void disconnectUser(String inviteCode, String telegramId){
        User user = userRepository.findByTelegramId(telegramId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Room room = getRoomByInviteCode(inviteCode);
        if (roomParticipants.containsKey(inviteCode)) {
           roomParticipants.get(inviteCode).remove(user);
            logger.info("User {} disconnected from room {}", telegramId, inviteCode);
        }
        if(roomReadyUsers.containsKey(inviteCode))
        {
            roomReadyUsers.get(inviteCode).remove(user.getTelegramId());
        }

    }

    public void deleteOldRooms() {
        // TODO implement delete rooms logic
    }
}