// hooks/useScrollFix.ts
import { useEffect } from "react";

const useScrollFix = () => {
  useEffect(() => {
    function ensureDocumentIsScrollable() {
      const isScrollable =
        document.documentElement.scrollHeight > window.innerHeight;
      if (!isScrollable) {
        document.documentElement.style.setProperty(
          "height",
          "calc(100vh + 1px)",
          "important"
        );
      }
    }

    function preventCollapse() {
      if (window.scrollY === 0) {
        window.scrollTo(0, 1);
      }
    }

    const scrollableElement = document.querySelector("#cardsWrapper"); // Замените на селектор вашего скроллабельного элемента
    if (scrollableElement) {
      scrollableElement.addEventListener("touchstart", preventCollapse);
      window.addEventListener("load", ensureDocumentIsScrollable);
      return () => {
        scrollableElement.removeEventListener("touchstart", preventCollapse);
        window.removeEventListener("load", ensureDocumentIsScrollable);
      };
    }
  }, []);
};

export default useScrollFix;
