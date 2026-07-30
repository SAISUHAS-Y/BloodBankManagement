import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';

export interface BloodRequestResponse {
  id: number;
  hospitalId: number;
  hospitalName?: string;
  bloodGroupId: number;
  bloodGroupLabel?: string;
  componentTypeCode?: string;
  componentTypeLabel?: string;
  unitsRequested: number;
  urgencyLevel: 'CRITICAL' | 'URGENT' | 'ROUTINE';
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED' | 'FULFILLED';
  requestDate?: string;
  createdAt?: string;
  reason?: string;
}

export interface BloodRequestDto {
  hospitalId: number;
  bloodGroupId: number;
  componentTypeCode?: string;
  unitsRequested: number;
  urgencyLevel: 'CRITICAL' | 'URGENT' | 'ROUTINE';
  reason?: string;
}

export interface IssuanceRequest {
  bloodRequestId: number;
  bloodBankId: number;
  unitsIssued: number;
  notes?: string;
}

export interface IssuanceResponse {
  id: number;
  bloodRequestId: number;
  bloodBankId: number;
  unitsIssued: number;
  issuedAt: string;
}

export const RequestApi = {
  list: (params?: { hospitalId?: number; bloodGroupId?: number; status?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.hospitalId) query.append('hospitalId', params.hospitalId.toString());
    if (params?.bloodGroupId) query.append('bloodGroupId', params.bloodGroupId.toString());
    if (params?.status) query.append('status', params.status);
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<BloodRequestResponse>>('GET', `/blood-requests${qs}`);
  },
  get: (id: number) => request<BloodRequestResponse>('GET', `/blood-requests/${id}`),
  create: (body: BloodRequestDto) => request<BloodRequestResponse>('POST', '/blood-requests', body),
  approve: (id: number) => request<BloodRequestResponse>('PATCH', `/blood-requests/${id}/approve`),
  reject: (id: number) => request<BloodRequestResponse>('PATCH', `/blood-requests/${id}/reject`),
  cancel: (id: number) => request<BloodRequestResponse>('PATCH', `/blood-requests/${id}/cancel`),
  issueBlood: (body: IssuanceRequest) => request<IssuanceResponse>('POST', '/issuances', body),
};
