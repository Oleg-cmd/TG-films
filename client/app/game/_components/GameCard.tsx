import React, { useState, Dispatch, SetStateAction, useEffect } from "react";
import Image from "next/image";
import { useMediaQuery } from "usehooks-ts";
import {
  motion,
  useMotionValue,
  useTransform,
  useMotionValueEvent,
} from "framer-motion";
import { themeColors } from "@/lib/theme";
import { type Card } from "@/types/games.type";
import { InformationCircleIcon } from "@heroicons/react/24/outline";
import { lobbyStore } from "@/store/lobbyStore";
import { toJS } from "mobx";

type Props = {
  data: Card;
  setCardDrivenProps: Dispatch<SetStateAction<any>>;
  setIsDragging: Dispatch<SetStateAction<any>>;
  isDragging: boolean;
  setIsDragOffBoundary: Dispatch<SetStateAction<any>>;
  setDirection: Dispatch<SetStateAction<any>>;
  onSwipe: (direction: "left" | "right", card: Card) => void;
};

const GameCard = ({
  data,
  setCardDrivenProps,
  setIsDragging,
  isDragging,
  setIsDragOffBoundary,
  setDirection,
  onSwipe,
}: Props) => {
  const [isFlipped, setIsFlipped] = useState(false);
  const x = useMotionValue(0);
  const isMobile = useMediaQuery("(max-width: 768px)");

  const offsetBoundary = 150;

  const inputX = [offsetBoundary * -1, 0, offsetBoundary];
  const outputX = [-200, 0, 200];
  const outputY = [50, 0, 50];
  const outputRotate = [-40, 0, 40];
  const outputActionScaleBadAnswer = [3, 1, 0.3];
  const outputActionScaleRightAnswer = [0.3, 1, 3];
  const outputMainBgColor = [
    themeColors.gameSwipe.left,
    themeColors.gameSwipe.neutral,
    themeColors.gameSwipe.right,
  ];

  let drivenX = useTransform(x, inputX, outputX);
  let drivenY = useTransform(x, inputX, outputY);
  let drivenRotation = useTransform(x, inputX, outputRotate);
  let drivenActionLeftScale = useTransform(
    x,
    inputX,
    outputActionScaleBadAnswer
  );
  let drivenActionRightScale = useTransform(
    x,
    inputX,
    outputActionScaleRightAnswer
  );
  // let drivenBg = useTransform(x, inputX, outputMainBgColor);
  let drivenBg = useTransform(x, [-20, 0, 20], outputMainBgColor);

  useMotionValueEvent(x, "change", (latest) => {
    //@ts-ignore
    setCardDrivenProps((state) => ({
      ...state,
      cardWrapperX: latest,
      buttonScaleBadAnswer: drivenActionLeftScale,
      buttonScaleGoodAnswer: drivenActionRightScale,
      mainBgColor: drivenBg,
    }));
  });

  const handleFlip = (e: React.MouseEvent) => {
    e.stopPropagation();
    setIsFlipped(!isFlipped);
  };
  return (
    <>
      <motion.div
        id={`cardDrivenWrapper-${data.id}`}
        className='absolute bg-white p-1 rounded-lg text-center w-full aspect-[110/150] pointer-events-none text-black origin-bottom shadow-card select-none'
        style={{
          y: drivenY,
          rotate: drivenRotation,
          x: drivenX,
        }}
      >
        <motion.div
          className='relative w-full h-full transition-transform duration-500'
          style={{ transformStyle: "preserve-3d" }}
          animate={{ rotateY: isFlipped ? 180 : 0 }}
          transition={{ duration: 0.05 }}
          onClick={(e) => {
            if (!isDragging) {
              handleFlip(e);
            }
          }}
        >
          <motion.div className='absolute w-full h-full backface-hidden rounded-lg'>
            <Image
              priority
              fill
              src={data.poster}
              alt={data.title}
              className='w-full object-cover rounded-lg'
            />
            <div className='absolute w-full bottom-0 right-0  bg-black bg-opacity-50 text-white p-4 rounded-lg'>
              <h2 className='text-2xl font-bold'>{data.title}</h2>
              <p className='text-lg'>
                {data.year} |{" "}
                {Array.isArray(data.genre) ? data.genre.join(", ") : data.genre}
              </p>
              <p className='text-lg'>
                Rating: {data.rating ? data.rating.toFixed(1) : "N/A"}/10
              </p>
            </div>
            <button
              onClick={handleFlip}
              className='absolute top-2 right-2 bg-white rounded-full p-2 shadow pointer-events-auto'
            >
              <InformationCircleIcon className='w-6 h-6 text-blue-600' />
            </button>
          </motion.div>

          {/* Back Side */}
          <motion.div className='absolute w-full top-0 right-0 h-full backface-hidden rotate-y-180 bg-white p-6 overflow-y-auto rounded-lg'>
            <h2 className='text-2xl font-bold mb-4'>{data.title}</h2>
            <p className='text-lg mb-4'>{data.description}</p>
            <button
              onClick={handleFlip}
              className='absolute top-2 left-2 bg-blue-600 text-white rounded-full p-2 shadow'
              style={{ zIndex: 999 }}
            >
              <InformationCircleIcon className='w-6 h-6' />
            </button>
          </motion.div>
        </motion.div>
      </motion.div>

      <motion.div
        id={`cardDriverWrapper-${data.id}`}
        className={`absolute top-[50px] w-full aspect-[110/150] ${
          !isDragging ? "hover:cursor-grab" : ""
        }`}
        drag='x'
        dragSnapToOrigin
        dragElastic={isMobile ? 0.2 : 0.06}
        dragConstraints={{ left: 0, right: 0 }}
        dragTransition={{ bounceStiffness: 1000, bounceDamping: 50 }}
        onDragStart={() => setIsDragging(true)}
        onDrag={(_, info) => {
          const offset = info.offset.x;

          if (offset < 0 && offset < offsetBoundary * -1) {
            setIsDragOffBoundary("left");
          } else if (offset > 0 && offset > offsetBoundary) {
            setIsDragOffBoundary("right");
          } else {
            setIsDragOffBoundary(null);
          }
        }}
        onDragEnd={(_, info) => {
          setIsDragging(false);
          setIsDragOffBoundary(null);
          const isOffBoundary =
            info.offset.x > offsetBoundary || info.offset.x < -offsetBoundary;
          const direction = info.offset.x > 0 ? "right" : "left";

          if (isOffBoundary) {
            setDirection(direction);
          }
          if (!isDragging) {
            onSwipe(direction, data);
          }
        }}
        style={{ x }}
      ></motion.div>
    </>
  );
};

export default GameCard;
