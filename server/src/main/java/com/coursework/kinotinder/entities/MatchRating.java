    package com.coursework.kinotinder.entities;

    import jakarta.persistence.*;
    import lombok.Getter;
    import lombok.Setter;
    import lombok.NoArgsConstructor;
    @Entity
    @Getter
    @Setter
    @NoArgsConstructor
    public class MatchRating {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long ratingId;

        @ManyToOne
        @JoinColumn(name = "match_id")
        private Match match;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        private Integer stars;

        public MatchRating(Match match, Integer stars) {
            this.match = match;
            this.stars = stars;
        }
    }