import type { PageResponse } from "../../shared/types/page";

export type Notification = {
  id: string;
  category: string;
  severity: string;
  title: string;
  message: string;
  actionPath: string;
  referenceType: string | null;
  referenceId: string | null;
  read: boolean;
  readAt: string | null;
  createdAt: string;
};

export type NotificationList = {
  unreadCount: number;
} & PageResponse<Notification>;
