const normalized = (value: string | undefined, fallback: string) => {
  const candidate = (value || "").trim().toLowerCase();
  return candidate || fallback;
};

export const appProfile = normalized(import.meta.env.VITE_APP_PROFILE, normalized(import.meta.env.MODE, "local"));

export const isLocalProfile = appProfile === "local";
