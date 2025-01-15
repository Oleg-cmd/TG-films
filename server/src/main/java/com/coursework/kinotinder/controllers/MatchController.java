package com.coursework.kinotinder.controllers;

    import com.coursework.kinotinder.entities.Match;
    import com.coursework.kinotinder.entities.MatchRating;
    import com.coursework.kinotinder.entities.User;
    import com.coursework.kinotinder.services.MatchService;
    import com.coursework.kinotinder.services.UserService;
    import java.util.List;
    import java.util.stream.Collectors;
    import org.springframework.web.bind.annotation.*;
    import lombok.RequiredArgsConstructor;

   @RestController
   @RequestMapping("/api/matches")
   @RequiredArgsConstructor
   public class MatchController {

       private final MatchService matchService;
       private final UserService userService;

       // Получение всех матчей
       @GetMapping("/{telegramId}")
       public List<MatchData> getAllMatches(@PathVariable String telegramId) {
           User user = userService.getUserByTelegramId(telegramId);
           if(user == null){
                throw new IllegalArgumentException("User not found");
           }
           List<Match> matches =  matchService.getAllMatches(user);

          return matches.stream().map(match -> {
               MatchRating rating = matchService.getMatchRatingForUser(match.getMatchId(), user);
               return new MatchData(
                 match,
                  rating != null ? rating.getStars() : 0
                );
            }).collect(Collectors.toList());

       }


       // Установка рейтинга матчу
       @PostMapping("/{matchId}/rate")
       public MatchRating rateMatch(
               @PathVariable Long matchId,
               @RequestParam String telegramId,
               @RequestParam Integer stars) {
           User user = userService.getUserByTelegramId(telegramId);
           if(user == null){
                throw new IllegalArgumentException("User not found");
           }
           return matchService.rateMatch(matchId, user, stars);
       }
   }

   record MatchData(Match match, Integer stars) {
   }