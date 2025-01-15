export type Game = {
  id: number;
  cards: Card[];
};

export type Card = {
  id: number;
  title: string;
  year: number;
  genre: string[];
  rating: number;
  poster: string;
  description: string;
};

export type CardSwipeDirection = "left" | "right";
export type IsDragOffBoundary = "left" | "right" | null;

export type Film = {
  id: number;
  title: string;
  year: number;
  genre: string[];
  rating: number;
  poster: string;
  description: string;
};

export type Match = {
  id: number;
  title: string;
  date: string;
  stats: {
    totalSwipes: number;
    totalTime: number;
    members: number;
  };
  rating: number;
  img: string;
};
export type MatchData = {
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
  stars?: number;
};
