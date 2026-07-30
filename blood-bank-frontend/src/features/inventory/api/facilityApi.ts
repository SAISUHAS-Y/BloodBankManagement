import { request } from '../../../api/http';

export interface PageResponse<T> {
  content: T[];
  pageNumber: number;
  pageSize: number;
  totalElements: number;
  totalPages: number;
  isLast: boolean;
}

export interface BloodBankResponse {
  id: number;
  name: string;
  licenseNumber: string;
  bloodBankTypeCode: string;
  bloodBankTypeLabel?: string;
  stateId: number;
  stateName?: string;
  districtId: number;
  districtName?: string;
  cityId: number;
  cityName?: string;
  addressLine?: string;
  phone?: string;
  email?: string;
  operatingHoursNote?: string;
  active: boolean;
}

export interface BloodBankRequest {
  name: string;
  licenseNumber: string;
  bloodBankTypeCode: string;
  stateId: number;
  districtId: number;
  cityId: number;
  addressLine?: string;
  phone?: string;
  email?: string;
  operatingHoursNote?: string;
}

export const FacilityApi = {
  list: (params?: { page?: number; size?: number; isActive?: boolean }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    if (params?.isActive !== undefined) query.append('isActive', params.isActive.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<BloodBankResponse>>('GET', `/blood-banks${qs}`);
  },
  get: (id: number) => request<BloodBankResponse>('GET', `/blood-banks/${id}`),
  create: (body: BloodBankRequest) => request<BloodBankResponse>('POST', '/blood-banks', body),
  update: (id: number, body: BloodBankRequest) => request<BloodBankResponse>('PUT', `/blood-banks/${id}`, body),
};
