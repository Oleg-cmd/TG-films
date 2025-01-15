"use client";
import React, { useState, Fragment, useEffect } from "react";
import Link from "next/link";
import { ArrowLeftIcon } from "@heroicons/react/24/solid";
import {
  TrophyIcon,
  FilmIcon,
  StarIcon,
  UserGroupIcon,
} from "@heroicons/react/24/outline";
import { Dialog, Transition } from "@headlessui/react";
import Rating from "./_components/Rating";
import { observer } from "mobx-react-lite";
import { lobbyStore } from "@/store/lobbyStore";
import axiosInstance from "../config/axios-config";

const Settings: React.FC = observer(() => {
  const [openMatchId, setOpenMatchId] = useState<number | null>(null);
  const [selectedMatch, setSelectedMatch] = useState<any>(null);

  const handleMatchClick = (match: any) => {
    setSelectedMatch(match);
    setOpenMatchId(match.match.matchId);
  };

  const handleCloseModal = () => {
    setOpenMatchId(null);
  };
  const handleRatingChange = (newRating: number) => {
    setSelectedMatch((prevMatch: any) => ({ ...prevMatch, stars: newRating }));
    if (selectedMatch) {
      rateMatch(selectedMatch.match.matchId, newRating);
    }
  };

  const rateMatch = async (matchId: number, stars: number) => {
    try {
      lobbyStore.sendMessage({
        type: "rate",
        payload: {
          matchId,
          stars,
          telegramId: lobbyStore.telegramId,
        },
      });
      const response = await axiosInstance.post(
        `/api/matches/${matchId}/rate?telegramId=${lobbyStore.telegramId}&stars=${stars}`
      );
      console.log(response.data);
    } catch (e) {
      console.log("Error setting rating:", e);
    }
  };

  useEffect(() => {
    document.documentElement.dataset.settingsPage = "true";
    if (!lobbyStore.isFetchingMatches && lobbyStore.telegramId) {
      lobbyStore.fetchMatches();
    }

    return () => {
      document.documentElement.dataset.settingsPage = "false";
    };
  }, [lobbyStore.telegramId]);

  return (
    <div className='min-h-screen bg-gradient-to-br from-pink-100 to-blue-100 p-6'>
      <Link
        href='/'
        className='inline-flex items-center text-blue-600 mb-6'
      >
        <ArrowLeftIcon className='w-5 h-5 mr-2' />
        Back to Swipe
      </Link>
      <h1 className='text-3xl font-bold mb-6'>User Settings</h1>
      <div className='bg-white rounded-lg shadow-xl p-6 mb-6'>
        <h2 className='text-2xl font-semibold mb-4 flex items-center gap-2'>
          Achievements
          <TrophyIcon className='w-6 h-6 text-gray-500' />
        </h2>
        <div className='grid grid-cols-1 md:grid-cols-3 gap-4'>
          <div className='bg-gray-100 p-4 rounded-lg shadow-md flex flex-col items-center justify-center'>
            <FilmIcon className='h-8 w-8 text-blue-600 mb-2' />
            <span className='font-semibold'>First Match</span>
          </div>
          <div className='bg-gray-100 p-4 rounded-lg shadow-md flex flex-col items-center justify-center'>
            <UserGroupIcon className='h-8 w-8 text-blue-600 mb-2' />
            <span className='font-semibold'>10 Matches</span>
          </div>
          <div className='bg-gray-100 p-4 rounded-lg shadow-md flex flex-col items-center justify-center'>
            <StarIcon className='h-8 w-8 text-blue-600 mb-2' />
            <span className='font-semibold'>100 Swipes</span>
          </div>
        </div>
      </div>
      <div className='bg-white rounded-lg shadow-xl p-6 mb-6'>
        <h2 className='text-2xl font-semibold mb-4 flex items-center gap-2'>
          Match History
          <FilmIcon className='w-6 h-6 text-gray-500' />
        </h2>
        <ul className='space-y-2'>
          {lobbyStore.matches?.map((match) => (
            <li
              key={match.match.matchId}
              className='bg-gray-100 p-4 rounded-lg shadow-md cursor-pointer hover:bg-gray-200 transition-colors'
              onClick={() => handleMatchClick(match)}
            >
              <div className='flex justify-between'>
                <span className='font-semibold'>
                  {match.match.movie?.title || "Movie Title"}
                </span>
                <span className='text-sm text-gray-500'>
                  {match.match.matchedAt}
                </span>
              </div>
            </li>
          ))}
        </ul>
        <Transition
          appear
          show={openMatchId !== null}
          as={Fragment}
        >
          <Dialog
            as='div'
            className='relative z-10'
            onClose={handleCloseModal}
          >
            <Transition.Child
              as={Fragment}
              enter='ease-out duration-300'
              enterFrom='opacity-0'
              enterTo='opacity-100'
              leave='ease-in duration-200'
              leaveFrom='opacity-100'
              leaveTo='opacity-0'
            >
              <div className='fixed inset-0 bg-black bg-opacity-25' />
            </Transition.Child>

            <div className='fixed inset-0 overflow-y-auto'>
              <div className='flex min-h-full items-center justify-center p-4 text-center'>
                <Transition.Child
                  as={Fragment}
                  enter='ease-out duration-300'
                  enterFrom='opacity-0 scale-95'
                  enterTo='opacity-100 scale-100'
                  leave='ease-in duration-200'
                  leaveFrom='opacity-100 scale-100'
                  leaveTo='opacity-0 scale-95'
                >
                  <Dialog.Panel className='w-full max-w-md transform overflow-hidden rounded-2xl bg-white p-6 text-left align-middle shadow-xl transition-all'>
                    <div className='flex flex-col items-center'>
                      {selectedMatch?.match.movie?.posterUrl && (
                        <img
                          src={selectedMatch?.match.movie?.posterUrl}
                          alt={
                            selectedMatch?.match.movie?.title || "Movie Title"
                          }
                          className='w-32 h-48 object-cover rounded-lg mb-4'
                        />
                      )}

                      <Dialog.Title
                        as='h3'
                        className='text-lg font-medium leading-6 text-gray-900 mb-4'
                      >
                        {selectedMatch?.match.movie?.title || "Movie Title"}
                      </Dialog.Title>
                      <p className='text-sm text-gray-600 mb-2'>
                        Matched on: {selectedMatch?.match.matchedAt}
                      </p>
                      <div className='flex justify-around w-full  mb-4'>
                        <div>
                          <p className='text-gray-600 text-sm text-center'>
                            Swipes
                          </p>
                          <p className='font-semibold text-xl text-center'>
                            {selectedMatch?.match.totalLikes}
                          </p>
                        </div>
                        <div>
                          <p className='text-gray-600 text-sm text-center'>
                            Time
                          </p>
                          <p className='font-semibold text-xl text-center'>
                            {selectedMatch?.match.sessionTime}s
                          </p>
                        </div>
                        <div>
                          <p className='text-gray-600 text-sm text-center'>
                            Members
                          </p>
                          <p className='font-semibold text-xl text-center'>
                            {selectedMatch?.stats?.members}
                          </p>
                        </div>
                      </div>

                      <Rating
                        currentRating={selectedMatch?.stars || 0}
                        onRatingChange={handleRatingChange}
                      />
                    </div>

                    <div className='mt-6 w-full flex justify-center'>
                      <button
                        type='button'
                        className='inline-flex justify-center rounded-md border border-transparent bg-blue-100 px-6 py-4 text-base font-medium text-blue-900 hover:bg-blue-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2'
                        onClick={handleCloseModal}
                      >
                        Close
                      </button>
                    </div>
                  </Dialog.Panel>
                </Transition.Child>
              </div>
            </div>
          </Dialog>
        </Transition>
      </div>
    </div>
  );
});

export default Settings;
