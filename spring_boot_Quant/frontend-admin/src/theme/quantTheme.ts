import { createTheme } from "@mui/material/styles";
import type { ThemeOptions } from "@mui/material/styles";

const sharedOptions: ThemeOptions = {
  shape: {
    borderRadius: 12
  },
  typography: {
    fontFamily:
      "\"Pretendard Variable\", \"Pretendard\", \"Noto Sans KR\", \"Apple SD Gothic Neo\", \"Segoe UI\", sans-serif",
    h5: {
      fontWeight: 700
    },
    h6: {
      fontWeight: 700
    },
    button: {
      fontWeight: 700,
      textTransform: "none"
    }
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10
        }
      }
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12
        }
      }
    },
    MuiDialog: {
      styleOverrides: {
        paper: {
          borderRadius: 14
        }
      }
    },
    MuiTableHead: {
      styleOverrides: {
        root: {
          "& .MuiTableCell-root": {
            fontWeight: 700
          }
        }
      }
    }
  }
};

export const quantTheme = createTheme({
  ...sharedOptions,
  palette: {
    mode: "light",
    primary: {
      main: "#33BCD7",
      dark: "#1EA4C1",
      light: "#63D1E5",
      contrastText: "#FFFFFF"
    },
    secondary: {
      main: "#1D9B59",
      dark: "#147742",
      light: "#56BA83",
      contrastText: "#FFFFFF"
    },
    background: {
      default: "#ECEFF5",
      paper: "#FFFFFF"
    },
    text: {
      primary: "#2A3851",
      secondary: "#5A6C8A"
    },
    success: {
      main: "#1D9B59"
    },
    warning: {
      main: "#D88C1B"
    },
    error: {
      main: "#C84343"
    }
  },
  sidebar: {
    width: 280
  },
  components: {
    ...sharedOptions.components,
    RaLayout: {
      styleOverrides: {
        root: {
          "& .RaLayout-content": {
            paddingTop: 8,
            paddingBottom: 8
          }
        }
      }
    },
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: {
          background: "#F4F6FB",
          color: "#2A3851",
          boxShadow: "none",
          borderBottom: "1px solid #D7E0EC"
        }
      }
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          background: "#082A56",
          borderRight: "none",
          color: "#D5E4FF"
        }
      }
    },
    RaAppBar: {
      styleOverrides: {
        toolbar: {
          minHeight: "62px !important",
          paddingLeft: "14px !important",
          paddingRight: "14px !important"
        },
        title: {
          fontWeight: 700
        }
      }
    },
    MuiPaper: {
      styleOverrides: {
        root: {
          borderRadius: 12
        }
      }
    }
  }
});

export const quantDarkTheme = createTheme({
  ...sharedOptions,
  palette: {
    mode: "dark",
    primary: {
      main: "#5C9DF2",
      dark: "#4B8EDB",
      light: "#88B9F7",
      contrastText: "#081423"
    },
    secondary: {
      main: "#40CACB",
      dark: "#1D9A9B",
      light: "#79E1E2"
    },
    background: {
      default: "#0B1421",
      paper: "#101D2F"
    },
    success: {
      main: "#43C677"
    },
    warning: {
      main: "#E3A34E"
    },
    error: {
      main: "#F16C6C"
    }
  },
  components: {
    ...sharedOptions.components,
    MuiAppBar: {
      styleOverrides: {
        colorPrimary: {
          background:
            "linear-gradient(120deg, #07101C 0%, #11253D 52%, #143A56 100%)"
        }
      }
    },
    MuiDrawer: {
      styleOverrides: {
        paper: {
          background: "#0D1929",
          borderRight: "1px solid #1E2F44"
        }
      }
    }
  }
});
