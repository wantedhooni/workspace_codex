"use client";

import { defaultDarkTheme, defaultLightTheme } from "react-admin";

export const lightTheme = {
  ...defaultLightTheme,
  palette: {
    ...(defaultLightTheme.palette ?? {}),
    primary: {
      main: "#0b3d91"
    },
    secondary: {
      main: "#1565c0"
    },
    background: {
      ...((defaultLightTheme.palette?.background ?? {}) as object),
      default: "#f5f7fb"
    }
  },
  shape: {
    borderRadius: 10
  }
};

export const darkTheme = {
  ...defaultDarkTheme,
  palette: {
    ...(defaultDarkTheme.palette ?? {}),
    primary: {
      main: "#90caf9"
    },
    secondary: {
      main: "#64b5f6"
    }
  }
};
