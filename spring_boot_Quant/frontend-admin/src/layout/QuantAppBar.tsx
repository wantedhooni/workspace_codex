import { Box, FormControl, IconButton, MenuItem, Select, Tooltip, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { AppBar, TitlePortal } from "react-admin";
import { GlobalCommandPalette } from "../components/GlobalCommandPalette";
import { usePortfolioCatalog } from "../portfolio/portfolioCatalog";
import { useSelectedPortfolioId } from "../portfolio/portfolioSelection";

export function QuantAppBar() {
  const [selectedPortfolioId, setSelectedPortfolioId] = useSelectedPortfolioId(1);
  const { choices } = usePortfolioCatalog();
  const [paletteOpen, setPaletteOpen] = useState(false);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === "k") {
        event.preventDefault();
        setPaletteOpen(true);
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, []);

  return (
    <>
      <AppBar color="primary">
        <TitlePortal />
        <Box sx={{ ml: 1.5, display: { xs: "none", lg: "flex" }, alignItems: "center" }}>
          <Typography variant="caption" color="inherit" sx={{ opacity: 0.72, letterSpacing: 0.3 }}>
            Quant Profit Ops Console
          </Typography>
        </Box>
        <Box sx={{ ml: "auto", display: { xs: "none", sm: "flex" }, alignItems: "center", minWidth: 0 }}>
          <Typography variant="caption" color="inherit" sx={{ mr: 1, opacity: 0.8, display: { xs: "none", md: "block" } }}>
            Active Portfolio
          </Typography>
          <FormControl size="small" sx={{ minWidth: { sm: 160, md: 220, lg: 280 }, maxWidth: 360 }}>
            <Select
              value={String(selectedPortfolioId)}
              onChange={(event) => setSelectedPortfolioId(Number(event.target.value))}
            >
              {choices.map((choice) => (
                <MenuItem key={choice.id} value={String(choice.id)}>
                  {choice.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </Box>
        <Box sx={{ ml: 1, display: { xs: "none", md: "flex" }, alignItems: "center" }}>
          <Tooltip title="Global Search (Ctrl+K)">
            <IconButton color="inherit" onClick={() => setPaletteOpen(true)}>
              <Typography variant="caption" fontWeight={700}>
                K
              </Typography>
            </IconButton>
          </Tooltip>
        </Box>
      </AppBar>
      <GlobalCommandPalette open={paletteOpen} onClose={() => setPaletteOpen(false)} />
    </>
  );
}
