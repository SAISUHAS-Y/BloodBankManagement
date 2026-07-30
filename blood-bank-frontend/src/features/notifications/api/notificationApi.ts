import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';

export interface NotificationLogResponse {
  id: number;
  recipientId: string;
  channel: 'EMAIL' | 'SMS' | 'IN_APP';
  eventType: string;
  subject?: string;
  content: string;
  status: 'SENT' | 'FAILED' | 'PENDING';
  sentAt?: string;
  errorMessage?: string;
}

export const NotificationApi = {
  getLogs: (params?: { recipientId?: string; status?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.recipientId) query.append('recipientId', params.recipientId);
    if (params?.status) query.append('status', params.status);
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<NotificationLogResponse>>('GET', `/notifications/logs${qs}`);
  },
};
