"use client";

import { BgPattern, Button } from "@/components/ui";
import SettingsScreen from "./game/_components/SettingsScreen";

export default function Home() {
  return (
    <main className='min-h-screen h-full mx-auto bg-gameSwipe-neutral'>
      <BgPattern />
      <SettingsScreen />
    </main>
  );
}
