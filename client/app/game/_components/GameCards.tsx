/* eslint-disable react-hooks/exhaustive-deps */
"use client";
import { useEffect, useState } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { motion, AnimatePresence } from "framer-motion";
import { X } from "lucide-react";

import { BgPattern } from "@/components/ui";
import { themeColors } from "@/lib/theme";

import { easeOutExpo } from "@/lib/easings.data";
import {
  Card,
  CardSwipeDirection,
  IsDragOffBoundary,
} from "@/types/games.type";
import GameCard from "./GameCard";
import GameActionBtn from "./GameActionBtn";
import { lobbyStore } from "@/store/lobbyStore";
import { observer } from "mobx-react-lite";
import { toJS } from "mobx";
import { usePathname } from "next/navigation";

const initialDrivenProps = {
  cardWrapperX: 0,
  buttonScaleBadAnswer: 1,
  buttonScaleGoodAnswer: 1,
  mainBgColor: themeColors.gameSwipe.neutral,
};

const GameCards = observer(() => {
  const [direction, setDirection] = useState<CardSwipeDirection | "">("");
  const [isDragOffBoundary, setIsDragOffBoundary] =
    useState<IsDragOffBoundary>(null);
  const [cardDrivenProps, setCardDrivenProps] = useState(initialDrivenProps);
  const [isDragging, setIsDragging] = useState(false);
  const cards = lobbyStore.cards;
  const router = useRouter();
  const pathname = usePathname();
  const handleDisconnect = () => {
    //@ts-ignore
    if (typeof window !== "undefined" && window.Telegram?.WebApp) {
      //@ts-ignore
      window.Telegram.WebApp.close();
    }
    lobbyStore.sendMessage({
      type: "disconnect",
      inviteCode: lobbyStore.inviteCode,
      telegramId: lobbyStore.telegramId,
    });
    router.push("/disconnected");
  };

  const handleSend = (direction: "left" | "right", card: Card) => {
    const liked = direction === "right" ? "like" : "dislike";
    console.log(liked);
    console.log(toJS(card));
    lobbyStore.sendMessage({
      type: "swipe",
      inviteCode: lobbyStore.inviteCode,
      payload: {
        telegramId: lobbyStore.telegramId,
        movieId: card.id,
        swipe: liked,
      },
    });
  };

  const handleActionBtnOnClick = (btn: CardSwipeDirection) => {
    setDirection(btn);
    if (cards.length > 0) {
      console.log("calling");
      handleSend(btn, cards[cards.length - 1]);
    } else {
      console.log("0 length");
    }
  };

  useEffect(() => {
    if (["left", "right"].includes(direction)) {
      lobbyStore.popCard();
    }

    setDirection("");
  }, [direction]);
  const cardVariants = {
    current: {
      opacity: 1,
      y: 0,
      scale: 1,
      transition: { duration: 0.3, ease: easeOutExpo },
    },
    upcoming: {
      opacity: 0.5,
      y: 67,
      scale: 0.9,
      transition: { duration: 0.3, ease: easeOutExpo, delay: 0 },
    },
    remainings: {
      opacity: 0,
      y: 20,
      scale: 0.9,
    },
    exit: {
      opacity: 0,
      x: direction === "left" ? -300 : 300,
      y: 40,
      rotate: direction === "left" ? -20 : 20,
      transition: { duration: 0.3, ease: easeOutExpo },
    },
  };

  return (
    <motion.div
      className={`flex p-5 min-h-screen h-full flex-col justify-center items-center overflow-hidden  ${
        isDragging ? "cursor-grabbing" : ""
      }`}
      style={{ backgroundColor: cardDrivenProps.mainBgColor }}
    >
      <BgPattern />
      <button
        onClick={handleDisconnect}
        id='close'
        className='absolute top-[20px] right-[20px] w-[30px] h-auto'
      >
        <X className='text-gray-500 w-full h-full' />
      </button>

      <div
        id='gameUIWrapper'
        className='flex flex-col gap-6 w-full items-center justify-center relative z-10'
      >
        <div
          id='cardsWrapper'
          className='w-full aspect-[100/150] max-w-xs mb-[20px] relative z-10'
        >
          <AnimatePresence>
            {cards.map((card: Card, i: number) => {
              const isLast = i === cards.length - 1;
              const isUpcoming = i === cards.length - 2;
              return (
                <div key={`card-${card.id}`}>
                  <motion.div
                    id={`card-${card.id}`}
                    className={`relative `}
                    variants={cardVariants}
                    initial='remainings'
                    animate={
                      isLast
                        ? "current"
                        : isUpcoming
                          ? "upcoming"
                          : "remainings"
                    }
                    exit='exit'
                  >
                    <GameCard
                      data={{
                        ...card,
                      }}
                      setCardDrivenProps={setCardDrivenProps}
                      setIsDragging={setIsDragging}
                      isDragging={isDragging}
                      setIsDragOffBoundary={setIsDragOffBoundary}
                      setDirection={setDirection}
                      onSwipe={handleSend}
                    />
                  </motion.div>
                </div>
              );
            })}
          </AnimatePresence>
        </div>
        <div
          id='actions'
          className='flex items-center justify-center w-full  gap-4 relative z-10'
        >
          <GameActionBtn
            direction='left'
            ariaLabel='swipe left'
            scale={cardDrivenProps.buttonScaleBadAnswer}
            isDragOffBoundary={isDragOffBoundary}
            onClick={() => handleActionBtnOnClick("left")}
          />
          <GameActionBtn
            direction='right'
            ariaLabel='swipe right'
            scale={cardDrivenProps.buttonScaleGoodAnswer}
            isDragOffBoundary={isDragOffBoundary}
            onClick={() => handleActionBtnOnClick("right")}
          />
        </div>
      </div>
    </motion.div>
  );
});

export default GameCards;
