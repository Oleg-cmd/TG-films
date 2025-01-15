// components/ui/WaitAnimation.tsx
"use client";

import React from "react";
import { motion } from "framer-motion";
import { X } from "lucide-react";
import Link from "next/link";
import { lobbyStore } from "@/store/lobbyStore";
import { observer } from "mobx-react-lite";

const WaitAnimation = observer(() => {
  return (
    <motion.div
      className='fixed inset-0 flex items-center justify-center z-50 bg-black bg-opacity-50'
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <Link
        href='/'
        id='close'
        className='absolute top-[20px] right-[20px] w-[30px] h-auto cursor-pointer z-50'
      >
        <X className='text-white w-full h-full' />
      </Link>

      <div className='flex flex-col justify-center items-center'>
        <motion.div
          className='text-3xl text-center font-bold text-white'
          initial={{ scale: 0.5, rotate: -10 }}
          animate={{ scale: 1, rotate: 0 }}
          transition={{ type: "spring", stiffness: 260, damping: 20 }}
        >
          You didnt pick a movie, most liked film:
        </motion.div>
        {lobbyStore.cards.length > 0 && (
          <div className='mt-8 flex flex-col justify-center items-center'>
            <div className='text-white font-bold text-4xl'>
              {lobbyStore.cards[0].title}
            </div>
            <div className='text-white font-bold text-2xl'>
              {lobbyStore.cards[0].year}
            </div>
          </div>
        )}
      </div>

      <motion.div
        className='absolute inset-0'
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5, duration: 2 }}
      >
        {[...Array(50)].map((_, i) => (
          <motion.div
            key={i}
            className='absolute w-2 h-2 bg-yellow-400 rounded-full'
            initial={{
              x:
                Math.random() *
                (typeof window !== "undefined" ? window.innerWidth : 0),
              y:
                Math.random() *
                (typeof window !== "undefined" ? window.innerHeight : 0),
              scale: 0,
            }}
            animate={{
              y: [
                null,
                Math.random() *
                  (typeof window !== "undefined" ? -window.innerHeight : 0),
              ],
              scale: [0, 1, 0],
            }}
            transition={{
              duration: Math.random() * 2 + 1,
              repeat: Infinity,
              repeatType: "loop",
            }}
          />
        ))}
      </motion.div>
    </motion.div>
  );
});

export default WaitAnimation;
