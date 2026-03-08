import { request, withQuery } from "../../shared/api/http";
import type { PaginationState } from "../../shared/types/page";
import type { Notification, NotificationList } from "./types";

export const notificationApi = {
  list(token: string, pagination: PaginationState) {
    return request<NotificationList>(withQuery("/api/user/notifications", pagination), {}, token);
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
