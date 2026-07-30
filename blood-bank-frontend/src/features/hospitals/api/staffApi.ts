import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';
import { StaffProfileResponse, StaffProfileRequest } from '../../users/api/userApi';

export const StaffApi = {
  list: (params?: { page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<StaffProfileResponse>>('GET', `/staff${qs}`);
  },
  get: (id: number) => request<StaffProfileResponse>('GET', `/staff/${id}`),
  create: (body: StaffProfileRequest) => request<StaffProfileResponse>('POST', '/staff', body),
  update: (id: number, body: StaffProfileRequest) => request<StaffProfileResponse>('PUT', `/staff/${id}`, body),
};
