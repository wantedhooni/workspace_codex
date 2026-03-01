import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import { Box, IconButton, Stack, Tooltip, Typography, type TypographyProps } from "@mui/material";

type PageTitleProps = {
  title: string;
  description: string;
  variant?: TypographyProps["variant"];
};

export function PageTitle({ title, description, variant = "h5" }: PageTitleProps) {
  return (
    <Stack direction="row" spacing={0.5} alignItems="center">
      <Typography variant={variant} fontWeight={700}>
        {title}
      </Typography>
      <Tooltip
        title={
          <Box sx={{ maxWidth: 320, py: 0.25 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 700 }}>
              화면 설명
            </Typography>
            <Typography variant="caption" sx={{ display: "block", mt: 0.5, lineHeight: 1.5 }}>
              {description}
            </Typography>
          </Box>
        }
        placement="right"
        arrow
        enterDelay={200}
      >
        <IconButton size="small" sx={{ color: "text.secondary" }}>
          <InfoOutlinedIcon fontSize="small" />
        </IconButton>
      </Tooltip>
    </Stack>
  );
}
