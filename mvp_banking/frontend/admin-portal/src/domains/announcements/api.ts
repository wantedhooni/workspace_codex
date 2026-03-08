import { request } from "../../shared/api/http";
import type { Announcement, CreateAnnouncementPayload, UpdateAnnouncementPayload } from "../../types";

export const adminAnnouncementsApi = {
  announcements(token: string) {
    return request<Announcement[]>("/api/admin/announcements", {}, token);
  },
  createAnnouncement(token: string, payload: CreateAnnouncementPayload) {
    return request<Announcement>(
      "/api/admin/announcements",
      {
        method: "POST",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  updateAnnouncement(token: string, announcementId: string, payload: UpdateAnnouncementPayload) {
    return request<Announcement>(
      `/api/admin/announcements/${announcementId}`,
      {
        method: "PUT",
        body: JSON.stringify(payload),
      },
      token,
    );
  },
  publishAnnouncement(token: string, announcementId: string) {
    return request<Announcement>(
      `/api/admin/announcements/${announcementId}/publish`,
      {
        method: "POST",
      },
      token,
    );
  },
  archiveAnnouncement(token: string, announcementId: string) {
    return request<Announcement>(
      `/api/admin/announcements/${announcementId}/archive`,
      {
        method: "POST",
      },
      token,
    );
  },
};
