// Design tokens below map 1:1 to docs/frontend-design-system.md section 1.
// If you change a color, radius, or font here, update that doc too — it's the
// single source of truth the whole team (and the presentation) refers back to.
import type { Config } from "tailwindcss";

const config: Config = {
  content: [
    "./app/**/*.{js,ts,jsx,tsx,mdx}",
    "./components/**/*.{js,ts,jsx,tsx,mdx}",
    "./lib/**/*.{js,ts,jsx,tsx,mdx}",
  ],
  theme: {
    extend: {
      colors: {
        ink: "#1C2333",
        paper: "#F5F6F4",
        line: "#D8DBD6",
        accent: "#0F6D66",
        muted: "#6B7280",
        surface: "#FFFFFF",
        status: {
          draft: "#8A8F98",
          submitted: "#3B6EA5",
          needsCorrection: "#B8701D",
          approved: "#2F7A4F",
        },
        feedback: {
          success: "#2F7A4F",
          error: "#B3392C",
          warning: "#B8701D",
          info: "#3B6EA5",
        },
      },
      borderRadius: {
        DEFAULT: "4px",
      },
      fontFamily: {
        sans: ["var(--font-plex-sans)"],
        mono: ["var(--font-plex-mono)"],
      },
    },
  },
};

export default config;
