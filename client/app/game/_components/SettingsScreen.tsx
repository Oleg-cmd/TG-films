// SettingsScreen.tsx
"use client";
import React, { useState, Fragment, useEffect } from "react";
import { Dialog, Transition } from "@headlessui/react";
import { FunnelIcon, UserIcon } from "@heroicons/react/24/outline";
import Link from "next/link";
import { observer } from "mobx-react-lite";
import { lobbyStore } from "@/store/lobbyStore";
import { useRouter } from "next/navigation";

const currentYear = new Date().getFullYear();

const SettingsScreen: React.FC = observer(() => {
  const router = useRouter();
  const [openGenreDialog, setOpenGenreDialog] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    if (lobbyStore.isLocalChange) {
      lobbyStore.setIsReady(false);
    }
    if (lobbyStore.startYear !== null) {
      setIsLoading(false);
    }
  }, [
    lobbyStore.selectedGenres,
    lobbyStore.startYear,
    lobbyStore.endYear,
    lobbyStore.rating,
    lobbyStore.isLocalChange,
  ]);

  const handleReady = () => {
    lobbyStore.setIsReady(!lobbyStore.isReady);
  };

  useEffect(() => {
    lobbyStore.sendMessage({
      type: "ready",
      telegramId: lobbyStore.telegramId,
      inviteCode: lobbyStore.inviteCode,
    });
  }, [lobbyStore.isReady]);

  useEffect(() => {
    if (
      lobbyStore.cards.length > 0 &&
      lobbyStore.readyCount === lobbyStore.participantsCount
    ) {
      console.log("GO TO GAME!");
      router.push("/game");
    }
  }, [lobbyStore.cards]);

  useEffect(() => {
    if (lobbyStore.errorMessage) {
      setErrorMessage(lobbyStore.errorMessage);
    }
  }, [lobbyStore.errorMessage]);

  if (isLoading) {
    return (
      <div className='p-4 flex flex-col justify-between bg-gradient-to-br from-blue-100 to-purple-100 rounded-lg shadow-lg h-screen'>
        <div className='flex justify-center items-center h-full'>
          Loading...
        </div>
      </div>
    );
  }

  return (
    <div className='p-4 flex flex-col justify-between bg-gradient-to-br from-blue-100 to-purple-100 rounded-lg shadow-lg h-screen'>
      <div className='space-y-6'>
        <div className='flex justify-between items-center pt-4'>
          <h1 className='text-3xl font-bold text-center text-blue-800'>
            Film Preferences
          </h1>
          <div className='flex justify-end'>
            <Link href='/settings'>
              <UserIcon className='w-8 h-8 text-blue-600 cursor-pointer' />
            </Link>
          </div>
        </div>

        <div className='space-y-4'>
          <h2 className='text-xl font-semibold text-blue-700 flex items-center gap-2'>
            Genres
            <button
              onClick={() => setOpenGenreDialog(true)}
              className='p-2 rounded-full bg-blue-200 hover:bg-blue-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus-ring-offset-1'
            >
              <FunnelIcon className='h-6 w-6 text-blue-700' />
            </button>
          </h2>

          <Transition
            appear
            show={openGenreDialog}
            as={Fragment}
          >
            <Dialog
              as='div'
              className='relative z-10'
              onClose={() => setOpenGenreDialog(false)}
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
                      <Dialog.Title
                        as='h3'
                        className='text-lg font-medium leading-6 text-gray-900 mb-4'
                      >
                        Select Genres
                      </Dialog.Title>
                      <div className='grid grid-cols-2 gap-3'>
                        {lobbyStore.availableGenres.map((genre) => (
                          <button
                            key={genre}
                            onClick={() => lobbyStore.toggleGenre(genre)}
                            className={`${
                              lobbyStore.selectedGenres.includes(genre)
                                ? "bg-blue-600 text-white"
                                : "bg-gray-200 text-gray-700"
                            } p-2 rounded-md shadow transition-colors w-full`}
                            style={{
                              pointerEvents: lobbyStore.isUpdatingSettings
                                ? "none"
                                : "auto",
                            }}
                          >
                            {genre}
                          </button>
                        ))}
                      </div>

                      <div className='mt-4'>
                        <button
                          type='button'
                          className='inline-flex justify-center rounded-md border border-transparent bg-blue-100 px-4 py-2 text-sm font-medium text-blue-900 hover:bg-blue-200 focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500 focus-visible:ring-offset-2'
                          onClick={() => setOpenGenreDialog(false)}
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

        <div className='space-y-4'>
          <h2 className='text-xl font-semibold text-blue-700'>Year Range</h2>
          <div className='flex flex-col space-y-2'>
            <label className='text-sm text-blue-600'>
              Start Year: {lobbyStore.startYear}
            </label>
            <input
              type='range'
              min={1900}
              max={currentYear}
              value={lobbyStore.startYear || 1900}
              onChange={(e) => {
                if (
                  lobbyStore.startYear !== null &&
                  !lobbyStore.isUpdatingSettings
                ) {
                  lobbyStore.setStartYear(parseInt(e.target.value));
                }
              }}
              onMouseUp={() => {
                if (!lobbyStore.isUpdatingSettings) {
                  lobbyStore.sendSettings();
                }
              }}
              onTouchEnd={() => {
                if (!lobbyStore.isUpdatingSettings) {
                  lobbyStore.sendSettings();
                }
              }}
              className='w-full h-2 bg-blue-200 rounded-lg appearance-none cursor-pointer'
              style={{
                pointerEvents: lobbyStore.isUpdatingSettings ? "none" : "auto",
              }}
            />
            <label className='text-sm text-blue-600'>
              End Year: {lobbyStore.endYear}
            </label>
            <input
              type='range'
              min={1900}
              max={currentYear}
              value={lobbyStore.endYear || currentYear}
              onChange={(e) => {
                if (
                  lobbyStore.endYear !== null &&
                  !lobbyStore.isUpdatingSettings
                ) {
                  lobbyStore.setEndYear(parseInt(e.target.value));
                }
              }}
              onMouseUp={() => {
                if (!lobbyStore.isUpdatingSettings) {
                  lobbyStore.sendSettings();
                }
              }}
              onTouchEnd={() => {
                if (!lobbyStore.isUpdatingSettings) {
                  lobbyStore.sendSettings();
                }
              }}
              className='w-full h-2 bg-blue-200 rounded-lg appearance-none cursor-pointer'
              style={{
                pointerEvents: lobbyStore.isUpdatingSettings ? "none" : "auto",
              }}
            />
          </div>
        </div>

        <div className='space-y-4'>
          <h2 className='text-xl font-semibold text-blue-700'>
            Minimum Rating
          </h2>
          <input
            type='range'
            min={0}
            max={10}
            step={0.1}
            value={lobbyStore.rating || 5}
            onChange={(e) => {
              if (
                lobbyStore.rating !== null &&
                !lobbyStore.isUpdatingSettings
              ) {
                lobbyStore.setRating(parseFloat(e.target.value));
              }
            }}
            onMouseUp={() => {
              if (!lobbyStore.isUpdatingSettings) {
                lobbyStore.sendSettings();
              }
            }}
            onTouchEnd={() => {
              if (!lobbyStore.isUpdatingSettings) {
                lobbyStore.sendSettings();
              }
            }}
            className='w-full h-2 bg-blue-200 rounded-lg appearance-none cursor-pointer'
            style={{
              pointerEvents: lobbyStore.isUpdatingSettings ? "none" : "auto",
            }}
          />
          <div className='text-center text-sm text-blue-600'>
            {lobbyStore.rating != null
              ? `${lobbyStore.rating?.toFixed(1)} / 10`
              : ""}
          </div>
        </div>
      </div>

      {errorMessage && (
        <div
          className='bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded relative'
          role='alert'
        >
          <strong className='font-bold'>Error!</strong>
          <span className='block sm:inline'> {errorMessage}</span>
        </div>
      )}

      <button
        onClick={handleReady}
        disabled={lobbyStore.selectedGenres.length === 0 || lobbyStore.isReady}
        className={`w-full py-3 px-4 rounded-lg mt-6 text-lg font-semibold transition-all transform hover:scale-105 ${
          lobbyStore.isReady
            ? "bg-gray-400 text-white cursor-not-allowed"
            : lobbyStore.selectedGenres.length === 0
              ? "bg-gray-200 text-gray-400 cursor-not-allowed"
              : "bg-blue-600 text-white hover:bg-blue-700 shadow-lg"
        }`}
      >
        {lobbyStore.isReady
          ? `Waiting for others... ${lobbyStore.readyCount || 0}/${lobbyStore.participantsCount || 1}`
          : "Ready to Swipe!"}
      </button>
    </div>
  );
});

export default SettingsScreen;
