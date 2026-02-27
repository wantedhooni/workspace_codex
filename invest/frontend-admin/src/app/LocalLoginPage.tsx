"use client";

import { Typography } from "@mui/material";
import { Login, LoginForm, PasswordInput, TextInput, required } from "react-admin";

const DEFAULT_DEMO_USERNAME = process.env.NEXT_PUBLIC_DEMO_USERNAME ?? "demo";
const DEFAULT_DEMO_PASSWORD = process.env.NEXT_PUBLIC_DEMO_PASSWORD ?? "demo1234";

function isLocalHost(): boolean {
  if (typeof window === "undefined") {
    return false;
  }

  return window.location.hostname === "localhost" || window.location.hostname === "127.0.0.1";
}

export default function LocalLoginPage() {
  const shouldPrefill = isLocalHost();

  return (
    <Login sx={{ background: "none" }}>
      <Typography sx={{ color: "text.secondary", textAlign: "center", mb: 1 }}>
        Demo login: {DEFAULT_DEMO_USERNAME} / {DEFAULT_DEMO_PASSWORD}
      </Typography>
      <LoginForm>
        <TextInput
          autoFocus
          source="username"
          label="Username"
          autoComplete="username"
          validate={required()}
          defaultValue={shouldPrefill ? DEFAULT_DEMO_USERNAME : undefined}
        />
        <PasswordInput
          source="password"
          label="Password"
          autoComplete="current-password"
          validate={required()}
          defaultValue={shouldPrefill ? DEFAULT_DEMO_PASSWORD : undefined}
        />
      </LoginForm>
    </Login>
  );
}
