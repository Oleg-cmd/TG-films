"use client";
import { makeAutoObservable, action, toJS } from "mobx";
import axios from "axios";
import { Card, Match } from "@/types/games.type";
import WebSocketService from "@/services/webSocketService";
import { v4 as uuidv4 } from "uuid";
import axiosInstance from "@/app/config/axios-config";
type MatchData = {
  match: {
    matchId: number;
    movie: {
      title: string;
      posterUrl: string;
    } | null;
    sessionTime: number;
    totalLikes: number;
    matchedAt: string;
  };
  stars: number;
};
class LobbyStore {
  selectedGenres: string[] = [];
  startYear: number | null = null;
  endYear: number | null = null;
  rating: number | null = null;
  isReady: boolean = false;
  readyCount: number | undefined = undefined;
  participantsCount: number | undefined = undefined;
  inviteCode: string = "J6a89W6J";
  telegramId: string = "";
  availableGenres: string[] = [];
  cards: Card[] = [];
  private webSocketService: WebSocketService;
  isUpdatingSettings: boolean = false;
  isInitialSettingsSet: boolean = false;
  isLocalChange: boolean = false;
  matchStatus: string = "none"; // 'wait', 'match', 'none'
  matchedMovie: any = null;
  errorMessage: string | null = null;
  matches: MatchData[] = [];
  isFetchingMatches: boolean = false;
  constructor() {
    this.telegramId = this.getOrCreateTelegramId();
    makeAutoObservable(this, {
      setInviteCode: action.bound,
      setSettings: action.bound,
      updateSettings: action.bound,
      toggleGenre: action.bound,
      setStartYear: action.bound,
      setEndYear: action.bound,
      setRating: action.bound,
      setIsReady: action.bound,
      setReadyCount: action.bound,
      setParticipantsCount: action.bound,
      setAvaliableGenres: action.bound,
      setCards: action.bound,
      updateCards: action.bound,
      setInitialSettings: action.bound,
      popCard: action.bound,
      setMatchStatus: action.bound,
      setMatchedMovie: action.bound,
      setErrorMessage: action.bound,
      fetchMatches: action.bound,
      setMatches: action.bound,
    });
    this.webSocketService = WebSocketService.getInstance();
    this.webSocketService.setOnConnectCallback(this.onConnected);
    this.connect();
  }
  setMatches(matches: MatchData[]) {
    this.matches = matches;
  }
  setErrorMessage(message: string | null) {
    this.errorMessage = message;
  }
  setMatchedMovie = (movie: any) => {
    this.matchedMovie = movie;
  };
  setMatchStatus = (status: string) => {
    this.matchStatus = status;
  };

  setInitialSettings = (settings: any) => {
    if (settings) {
      this.selectedGenres = settings.selectedGenres;
      this.startYear = settings.startYear;
      this.endYear = settings.endYear;
      this.rating = settings.rating;
      this.isInitialSettingsSet = true;
    }
  };

  private getOrCreateTelegramId(): string {
    if (typeof window === "undefined") {
      return "server_side_user";
    }
    let telegramId = localStorage.getItem("telegramId");
    if (!telegramId) {
      // telegramId = uuidv4();
      telegramId = "407444255";
      localStorage.setItem("telegramId", telegramId);
    }
    return telegramId;
  }
  connect = () => {
    console.log("Connecting...");

    // Получаем параметры из URL
    const urlParams =
      typeof window !== "undefined"
        ? new URLSearchParams(window.location.search)
        : null;
    const inviteCode = urlParams?.get("inviteCode");
    const telegramId = urlParams?.get("telegramId");
    if (inviteCode) {
      this.inviteCode = inviteCode;
      console.log("inviteCode from url:", inviteCode);
    }

    if (telegramId) {
      this.telegramId = telegramId;
      console.log(`Telegram id of user is: ${telegramId}`);
    }

    this.webSocketService.connect(); // Подключаемся только один раз
  };
  onConnected = () => {
    console.log("Connected to WebSocket");
    this.webSocketService.subscribe("/topic/connect", (message) => {
      console.log("Received connect message:", message);
      try {
        const parsedMessage = JSON.parse(message.body);
        if (parsedMessage.inviteCode) {
          console.log("connect message:", parsedMessage);
          this.setInviteCode(parsedMessage.inviteCode);
        }
      } catch (e) {
        console.error("Error parsing connect message:", e);
      }
    });

    // Subscribe to room settings
    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/settings`,
      (message) => {
        console.log("Settings message received before try:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          console.log("Settings message received:", parsedMessage);
          if (!this.startYear) {
            this.setInitialSettings(parsedMessage);
          } else {
            this.updateSettings(parsedMessage);
          }
        } catch (e) {
          console.error("Error parsing settings message:", e);
        }
      }
    );

    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/ready`,
      (message) => {
        console.log("Ready message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          this.setReadyCount(parsedMessage.readyCount);
          this.setParticipantsCount(parsedMessage.participantsCount);
          console.log("ready message parsed:", parsedMessage);
        } catch (e) {
          console.error("Error parsing ready message:", e);
        }
      }
    );

    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/match`,
      (message) => {
        console.log("Match message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          this.setMatchStatus("match");
          this.setMatchedMovie(parsedMessage);
          console.log("match message parsed:", parsedMessage);
        } catch (e) {
          console.error("Error parsing match message:", e);
        }
      }
    );

    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/wait`,
      (message) => {
        console.log("wait message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          this.setMatchStatus("wait");
          this.setMatchedMovie(parsedMessage);
          console.log("wait message parsed:", parsedMessage);
        } catch (e) {
          console.error("Error parsing wait message:", e);
        }
      }
    );
    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/movies`,
      (message) => {
        console.log("Movies message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          if (parsedMessage.movies && Array.isArray(parsedMessage.movies)) {
            const movies = parsedMessage.movies.map((movie: any) => {
              return {
                id: movie.movieId,
                title: movie.title,
                year: movie.releaseYear,
                genre: movie.genres,
                rating: movie.externalRating,
                poster: movie.posterUrl,
                description: movie.description,
              };
            });
            this.updateCards([...this.cards, ...movies]);
          } else {
            console.error("No movies found in message:", parsedMessage);
          }
        } catch (error) {
          console.error(
            "Error processing movies message:",
            error,
            message.body
          );
        }
      }
    );

    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/events`,
      (message) => {
        console.log("Event message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          if (
            typeof parsedMessage === "object" &&
            parsedMessage !== null &&
            parsedMessage.message &&
            parsedMessage.message === "No movies found for selected filters"
          ) {
            console.log("error message parsed:", parsedMessage);
            this.setErrorMessage(parsedMessage.message);
            return;
          }
          if (
            typeof parsedMessage === "object" &&
            parsedMessage !== null &&
            parsedMessage.message &&
            parsedMessage.message.includes("Game started")
          ) {
            console.log("Game started message parsed:", parsedMessage);
          }
        } catch (error) {
          console.error("Error processing event message:", error, message.body);
        }
      }
    );

    this.webSocketService.subscribe(
      `/topic/rooms/${this.inviteCode}/rating`,
      (message) => {
        console.log("Rating message received:", message);
        try {
          const parsedMessage = JSON.parse(message.body);
          this.updateMatchRating(
            parsedMessage.matchId,
            parsedMessage.telegramId,
            parsedMessage.stars
          );
          console.log("rating message parsed:", parsedMessage);
        } catch (e) {
          console.error("Error parsing rating message:", e);
        }
      }
    );

    // join event
    if (this.inviteCode && this.telegramId !== "server_side_user") {
      this.sendMessage({
        type: "join",
        telegramId: this.telegramId,
        inviteCode: this.inviteCode,
      });
    }

    this.getGenres();
  };

  updateMatchRating = (matchId: number, telegramId: string, stars: number) => {
    this.matches = this.matches.map((matchData) => {
      if (matchData.match.matchId === matchId) {
        return {
          ...matchData,
          stars: stars,
        };
      }
      return matchData;
    });
  };

  onError = (error: any) => {
    console.error("Error connecting to WebSocket:", error);
  };
  setInviteCode(inviteCode: string) {
    this.inviteCode = inviteCode;
  }

  setSettings(settings: any) {
    this.isUpdatingSettings = true;
    console.log("setSettings called with:", settings);
    this.selectedGenres = settings?.selectedGenres;
    this.startYear = settings?.startYear;
    this.endYear = settings?.endYear;
    this.rating = settings?.rating;
    this.isUpdatingSettings = false;
  }
  updateSettings(settings: any) {
    if (!this.isInitialSettingsSet) {
      return;
    }
    this.isUpdatingSettings = true;
    this.selectedGenres = settings?.selectedGenres;
    this.startYear = settings?.startYear;
    this.endYear = settings?.endYear;
    this.rating = settings?.rating;
    this.isUpdatingSettings = false;
  }

  sendMessage = (message: any) => {
    let payload;
    let destination = this.getDestination(message.inviteCode, message.type);
    if (message.type === "connect") {
      payload = message.telegramId;
      this.webSocketService.send(destination, payload);
    } else if (message.type === "join") {
      if (this.telegramId === "server_side_user") {
        return;
      }
      payload = message.telegramId;
      this.webSocketService.send(destination, payload);
    } else if (message.type === "settings") {
      payload = JSON.stringify({
        selectedGenres: this.selectedGenres,
        startYear: this.startYear,
        endYear: this.endYear,
        rating: this.rating,
      });
      this.webSocketService.send(destination, payload);
    } else if (message.type === "ready") {
      if (this.telegramId === "server_side_user") {
        return;
      }
      payload = JSON.stringify({
        telegramId: message.telegramId,
        isReady: this.isReady,
      });
      this.webSocketService.send(destination, payload);
    } else if (message.type === "swipe") {
      payload = JSON.stringify(message.payload);
      this.webSocketService.send(destination, payload);
    } else if (message.type === "disconnect") {
      payload = JSON.stringify({
        telegramId: message.telegramId,
      });
      this.webSocketService.send(destination, payload);
    } else if (message.type === "rate") {
      payload = JSON.stringify(message.payload);
      this.webSocketService.send(destination, payload);
    }
    console.log("Sent: " + JSON.stringify(message));
  };

  popCard = () => {
    if (this.cards.length === 3) {
      this.getNextBatch();
    }
    this.cards.pop();
    console.log(toJS(this.cards));
  };

  getDestination(inviteCode: string | null, type: string) {
    if (type === "connect") {
      return "/app/connect";
    } else if (type === "join") {
      return `/app/rooms/${inviteCode}/join`;
    } else if (type === "settings") {
      return `/app/rooms/${inviteCode}/settings`;
    } else if (type === "ready") {
      return `/app/rooms/${inviteCode}/ready`;
    } else if (type === "swipe") {
      return `/app/rooms/${inviteCode}/swipe`;
    } else if (type === "disconnect") {
      return `/app/rooms/${inviteCode}/disconnect`;
    } else if (type === "rate") {
      return `/app/matches/rate`;
    }
    return "";
  }
  toggleGenre(genre: string) {
    if (this.isUpdatingSettings || !this.isInitialSettingsSet) {
      return;
    }
    this.isLocalChange = true;
    this.selectedGenres = this.selectedGenres.includes(genre)
      ? this.selectedGenres.filter((g) => g !== genre)
      : [...this.selectedGenres, genre];
    this.sendSettings();
    this.isReady = false;
    this.isLocalChange = false;
  }

  setStartYear(year: number) {
    if (this.isUpdatingSettings || !this.isInitialSettingsSet) {
      return;
    }
    this.isLocalChange = true;
    this.startYear = year;
    this.isReady = false;
    this.isLocalChange = false;
  }

  setEndYear(year: number) {
    if (this.isUpdatingSettings || !this.isInitialSettingsSet) {
      return;
    }
    this.isLocalChange = true;
    this.endYear = year;
    this.isReady = false;
    this.isLocalChange = false;
  }

  setRating(rating: number) {
    if (this.isUpdatingSettings || !this.isInitialSettingsSet) {
      return;
    }
    this.isLocalChange = true;
    this.rating = rating;
    this.sendSettings();
    this.isReady = false;
    this.isLocalChange = false;
  }

  sendSettings = () => {
    this.sendMessage({
      type: "settings",
      inviteCode: this.inviteCode,
    });
  };

  setIsReady(isReady: boolean) {
    this.isReady = isReady;
    if (isReady) {
      this.setErrorMessage(null);
    }
  }

  setReadyCount(readyCount: number | undefined) {
    this.readyCount = readyCount;
  }

  setParticipantsCount(participantsCount: number | undefined) {
    this.participantsCount = participantsCount;
  }

  setAvaliableGenres(genres: string[]) {
    this.availableGenres = genres;
  }
  updateCards(cards: Card[]) {
    this.cards = cards;
    console.log(cards);
  }
  setCards(cards: Card[]) {
    this.cards = cards;
  }
  getNextBatch = async () => {
    try {
      this.webSocketService.send(`/app/rooms/${this.inviteCode}/movies`, "");
      console.log("get next batch");
    } catch (e) {
      console.log("Error get next batch:", e);
    }
  };

  fetchMatches = async () => {
    this.isFetchingMatches = true;
    try {
      const response = await axiosInstance.get(
        `/api/matches/${this.telegramId}`
      );
      console.log(response.data);
      const matches = response.data.map((matchData: any) => {
        const date = new Date(matchData.match.matchedAt);
        const formattedDate = date.toLocaleDateString("en-US", {
          year: "numeric",
          month: "long",
          day: "numeric",
        });
        return {
          match: {
            matchId: matchData.match.matchId,
            movie: matchData.match.movie,
            sessionTime: matchData.match.sessionTime,
            totalLikes: matchData.match.totalLikes,
            matchedAt: formattedDate,
            membersCount: matchData.match.membersCount,
            totalSwipes: matchData.match.totalSwipes,
          },
          stars: matchData.stars,
        };
      });
      this.setMatches(matches);
    } catch (e) {
      console.log("Error fetching matches:", e);
    } finally {
      this.isFetchingMatches = false;
    }
  };

  getGenres = async () => {
    try {
      const response = await axiosInstance.get(`/api/genres`);
      this.setAvaliableGenres(response.data.map((genre: any) => genre.name));
      console.log("got genres");
    } catch (e) {
      console.log("Error fetching genres:", e);
    }
  };
}

export const lobbyStore = new LobbyStore();
