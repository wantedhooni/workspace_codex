import type { Config } from "tailwindcss";

const config: Config = {
  content: ["./src/**/*.{ts,tsx}"],
  theme: {
    extend: {
      colors: {
        background: "#f5efe4",
        foreground: "#10212b",
        card: "#fffdf8",
        border: "#dfd4bf",
        primary: "#123524",
        accent: "#c87438",
        muted: "#f1e7d5",
        success: "#0f766e",
        danger: "#b91c1c",
      },
      boxShadow: {
        panel: "0 24px 80px rgba(16, 33, 43, 0.08)",
      },
      borderRadius: {
        xl: "1.25rem",
      },
      fontFamily: {
        sans: ["var(--font-manrope)", "sans-serif"],
      },
      backgroundImage: {
        "market-grid":
          "linear-gradient(rgba(18, 53, 36, 0.06) 1px, transparent 1px), linear-gradient(90deg, rgba(18, 53, 36, 0.06) 1px, transparent 1px)",
      },
    },
  },
  plugins: [],
};

export default config;
