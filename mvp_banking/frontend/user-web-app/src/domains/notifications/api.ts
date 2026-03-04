import { request } from "../../shared/api/http";
import type { Notification, NotificationList } from "./types";

export const notificationApi = {
  list(token: string) {
    return request<NotificationList>("/api/user/notifications", {}, token);
  },
  markRead(token: string, notificationId: string) {
    return request<Notification>(
      `/api/user/notifications/${notificationId}/read`,
      {
        method: "POST",
      },
      token,
    );
  },
};
