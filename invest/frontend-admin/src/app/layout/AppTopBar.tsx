"use client";

import InsightsIcon from "@mui/icons-material/Insights";
import { Box, Typography, useMediaQuery, type Theme } from "@mui/material";
import { AppBar, TitlePortal } from "react-admin";

export default function AppTopBar() {
  const isLargeEnough = useMediaQuery<Theme>((theme) => theme.breakpoints.up("sm"));

  return (
    <AppBar color="secondary">
      <TitlePortal />
      {isLargeEnough ? (
        <>
          <Box sx={{ flex: 1 }} />
          <InsightsIcon sx={{ mr: 1 }} />
          <Typography variant="body1" sx={{ fontWeight: 600 }}>
            Quant Admin
          </Typography>
        </>
      ) : null}
    </AppBar>
  );
}
