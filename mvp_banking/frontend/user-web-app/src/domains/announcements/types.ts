export type Announcement = {
  id: string;
  title: string;
  summary: string;
  body: string;
  severity: string;
  audience: string;
  status: string;
  pinned: boolean;
  startsAt: string | null;
  endsAt: string | null;
  publishedAt: string | null;
  archivedAt: string | null;
  createdByEmail: string;
  createdAt: string;
};
