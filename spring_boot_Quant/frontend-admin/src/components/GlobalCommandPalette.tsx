import {
  Box,
  Chip,
  CircularProgress,
  Dialog,
  DialogContent,
  DialogTitle,
  Divider,
  List,
  ListItemButton,
  ListItemText,
  Stack,
  TextField,
  Typography
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { useDataProvider, useNotify } from "react-admin";

type SearchItem = {
  resourceKey: string;
  itemKey: string;
  title: string;
  subtitle?: string;
  path: string;
  score: number;
};

type SearchSection = {
  resourceKey: string;
  resourceLabel: string;
  totalCount: number;
  items: SearchItem[];
};

type SearchResponseRow = {
  id: string;
  query: string;
  limit: number;
  sections: SearchSection[];
};

type FlatRow = {
  sectionLabel: string;
  item: SearchItem;
};

type GlobalCommandPaletteProps = {
  open: boolean;
  onClose: () => void;
};

export function GlobalCommandPalette({ open, onClose }: GlobalCommandPaletteProps) {
  const dataProvider = useDataProvider();
  const notify = useNotify();
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(false);
  const [sections, setSections] = useState<SearchSection[]>([]);
  const [activeIndex, setActiveIndex] = useState(0);

  useEffect(() => {
    if (!open) {
      setQuery("");
      setSections([]);
      setLoading(false);
      setActiveIndex(0);
    }
  }, [open]);

  useEffect(() => {
    if (!open) {
      return;
    }

    const timer = window.setTimeout(async () => {
      try {
        setLoading(true);
        const result = await dataProvider.getList<SearchResponseRow>("globalSearch", {
          pagination: { page: 1, perPage: 1 },
          sort: { field: "score", order: "DESC" },
          filter: { q: query, limit: 24 }
        });
        const row = (result.data?.[0] ?? null) as SearchResponseRow | null;
        setSections(Array.isArray(row?.sections) ? row.sections : []);
        setActiveIndex(0);
      } catch (error) {
        notify(error instanceof Error ? error.message : "글로벌 검색 실패", { type: "warning" });
      } finally {
        setLoading(false);
      }
    }, 180);

    return () => window.clearTimeout(timer);
  }, [dataProvider, notify, open, query]);

  const flatRows = useMemo<FlatRow[]>(
    () =>
      sections.flatMap((section) =>
        (section.items ?? []).map((item) => ({
          sectionLabel: section.resourceLabel,
          item
        }))
      ),
    [sections]
  );

  useEffect(() => {
    if (!open) return;

    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "ArrowDown") {
        event.preventDefault();
        if (flatRows.length === 0) return;
        setActiveIndex((prev) => (prev + 1) % flatRows.length);
      } else if (event.key === "ArrowUp") {
        event.preventDefault();
        if (flatRows.length === 0) return;
        setActiveIndex((prev) => (prev - 1 + flatRows.length) % flatRows.length);
      } else if (event.key === "Enter") {
        if (flatRows.length === 0) return;
        event.preventDefault();
        go(flatRows[activeIndex]?.item);
      } else if (event.key === "Escape") {
        onClose();
      }
    };

    window.addEventListener("keydown", onKeyDown);
    return () => window.removeEventListener("keydown", onKeyDown);
  }, [activeIndex, flatRows, onClose, open]);

  const go = (item: SearchItem | undefined) => {
    if (!item) {
      return;
    }
    onClose();

    const path = String(item.path ?? "");
    if (!path) {
      return;
    }

    if (path.startsWith("/#/")) {
      window.location.hash = path.slice(1);
      return;
    }
    window.location.assign(path);
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle sx={{ pb: 1 }}>Global Search</DialogTitle>
      <DialogContent sx={{ pt: 0.5 }}>
        <Stack spacing={1.25}>
          <TextField
            autoFocus
            placeholder="메뉴/주문/체결/포지션/사용자 검색 (Ctrl+K)"
            value={query}
            onChange={(event) => setQuery(event.target.value)}
            fullWidth
          />

          <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
            <Chip size="small" variant="outlined" label="Enter: 이동" />
            <Chip size="small" variant="outlined" label="↑/↓: 선택" />
            <Chip size="small" variant="outlined" label="Esc: 닫기" />
            {loading ? <CircularProgress size={16} /> : null}
          </Stack>

          <Divider />

          {flatRows.length === 0 && !loading ? (
            <Typography color="text.secondary" variant="body2">
              검색 결과가 없습니다.
            </Typography>
          ) : (
            <List dense sx={{ maxHeight: 420, overflowY: "auto" }}>
              {sections.map((section) => (
                <Box key={section.resourceKey}>
                  <Typography variant="caption" color="text.secondary" sx={{ px: 1.5 }}>
                    {section.resourceLabel} ({section.totalCount})
                  </Typography>
                  {(section.items ?? []).map((item) => {
                    const idx = flatRows.findIndex((row) => row.item.itemKey === item.itemKey);
                    const selected = idx === activeIndex;
                    return (
                      <ListItemButton key={item.itemKey} selected={selected} onClick={() => go(item)}>
                        <ListItemText
                          primary={item.title}
                          secondary={
                            <Stack direction="row" spacing={1} alignItems="center" useFlexGap flexWrap="wrap">
                              <Typography variant="caption" color="text.secondary">
                                {item.subtitle}
                              </Typography>
                              <Chip size="small" label={item.resourceKey} />
                            </Stack>
                          }
                        />
                      </ListItemButton>
                    );
                  })}
                  <Divider />
                </Box>
              ))}
            </List>
          )}
        </Stack>
      </DialogContent>
    </Dialog>
  );
}
