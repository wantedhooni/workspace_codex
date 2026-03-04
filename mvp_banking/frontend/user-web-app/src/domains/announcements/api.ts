import { request } from "../../shared/api/http";
import type { Announcement } from "./types";

export const announcementApi = {
  list(token: string) {
    return request<Announcement[]>("/api/user/announcements", {}, token);
  },
};
