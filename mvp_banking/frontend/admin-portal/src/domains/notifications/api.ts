import { request } from "../../shared/api/http";
import type { Notification, NotificationList } from "../../types";

export const adminNotificationsApi = {
  notifications(token: string) {
    return request<NotificationList>("/api/admin/notifications", {}, token);
  },
  markNotificationRead(token: string, notificationId: string) {
    return request<Notification>(
      `/api/admin/notifications/${notificationId}/read`,
      {
        method: "POST",
      },
      token,
    );
  },
};
