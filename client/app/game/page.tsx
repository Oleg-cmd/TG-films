// components/Game.tsx
"use client";
import { motion, AnimatePresence, cubicBezier } from "framer-motion";
import GameCards from "../game/_components/GameCards";
import MatchAnimation from "./_components/MatchAnimation";
import { lobbyStore } from "@/store/lobbyStore";
import { observer } from "mobx-react-lite";
import WaitAnimation from "./_components/WaitAnimation";

const Game = observer(() => {
  const isCardStockEmpty = lobbyStore.cards.length === 0;
  const gameScreenVariants = {
    initial: {
      opacity: 0,
    },
    animate: {
      opacity: 1,
      transition: { duration: 2, ease: cubicBezier(0.16, 1, 0.3, 1) },
    },
    exit: {
      opacity: 0,
      transition: { duration: 0.2, ease: cubicBezier(0.7, 0, 0.84, 0) },
    },
  };

  return (
    <main className='min-h-screen h-full mx-auto bg-gameSwipe-neutral'>
      <AnimatePresence mode='wait'>
        {lobbyStore.matchStatus === "match" ? (
          <motion.div
            key='gameScreen2'
            id='gameCompletion'
            variants={gameScreenVariants}
            initial='initial'
            animate='animate'
            exit='exit'
          >
            <MatchAnimation />
          </motion.div>
        ) : !isCardStockEmpty ? (
          <motion.div
            key='gameScreen1'
            id='gameScreen'
            variants={gameScreenVariants}
            initial='initial'
            animate='animate'
            exit='exit'
          >
            <GameCards />
          </motion.div>
        ) : (
          <motion.div
            key='gameScreen3'
            id='gameCompletion'
            variants={gameScreenVariants}
            initial='initial'
            animate='animate'
            exit='exit'
          >
            <WaitAnimation />
          </motion.div>
        )}
      </AnimatePresence>
    </main>
  );
});

export default Game;
