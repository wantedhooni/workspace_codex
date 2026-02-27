import { createTheme } from '@mui/material/styles';

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#0C4A6E' },
    secondary: { main: '#EA580C' },
    background: {
      default: '#F7F3EE',
      paper: '#FFFFFF',
    },
    text: {
      primary: '#0B1B2B',
      secondary: '#4B5563',
    },
  },
  typography: {
    fontFamily: '"Space Grotesk", "IBM Plex Mono", sans-serif',
    h1: { fontWeight: 700 },
    h2: { fontWeight: 700 },
    h3: { fontWeight: 600 },
    button: { textTransform: 'none', fontWeight: 600 },
  },
  shape: {
    borderRadius: 16,
  },
  components: {
    MuiAppBar: {
      styleOverrides: {
        root: { backgroundColor: '#0B1B2B' },
      },
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          border: '1px solid rgba(15, 23, 42, 0.08)',
        },
      },
    },
  },
});
