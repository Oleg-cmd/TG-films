import React, { useState } from "react";
import {
  StarIcon as OutlineStarIcon,
  StarIcon as SolidStarIcon,
} from "@heroicons/react/24/solid";

interface RatingProps {
  currentRating: number;
  onRatingChange: (rating: number) => void;
}

const Rating: React.FC<RatingProps> = ({ currentRating, onRatingChange }) => {
  const [hoveredRating, setHoveredRating] = useState<number | null>(null);

  const handleRatingClick = (rating: number) => {
    onRatingChange(rating);
  };

  const handleStarHover = (rating: number | null) => {
    setHoveredRating(rating);
  };

  const stars = Array.from({ length: 5 }, (_, i) => i + 1);

  return (
    <div className='flex items-center gap-1'>
      {stars.map((star) => {
        const isFilled =
          hoveredRating !== null
            ? star <= hoveredRating
            : star <= currentRating;
        return (
          <button
            key={star}
            onClick={() => handleRatingClick(star)}
            onMouseEnter={() => handleStarHover(star)}
            onMouseLeave={() => handleStarHover(null)}
            className='focus:outline-none'
          >
            {isFilled ? (
              <SolidStarIcon className='h-10 w-10 text-yellow-400' />
            ) : (
              <OutlineStarIcon className='h-10 w-10 text-gray-400' />
            )}
          </button>
        );
      })}
    </div>
  );
};

export default Rating;
