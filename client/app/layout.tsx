"use client";
// app/layout.tsx
import "../styles/globals.css";
import { init } from "@telegram-apps/sdk-react";
import { useEffect } from "react";
import useScrollFix from "./useScrollFix";

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  useScrollFix();
  useEffect(() => {
    if (typeof window !== "undefined") {
      try {
        init();
        if (typeof window !== "undefined") {
          console.log("Telegram Web Apps SDK initialized successfully");
        } else {
          console.error("Telegram Web Apps SDK initialization failed");
        }
      } catch (e) {
        console.error("Error init Telegram Web Apps SDK:", e);
      }
    } else {
      console.log("Not in Telegram Web Apps");
    }
  }, []);
  return (
    <html lang='en'>
      <body>{children}</body>
    </html>
  );
}
