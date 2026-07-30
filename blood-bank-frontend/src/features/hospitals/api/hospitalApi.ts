import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';

export interface HospitalContactResponse {
  id?: number;
  contactPersonName: string;
  designation: string;
  phone: string;
  email?: string;
  primaryContact: boolean;
}

export interface HospitalResponse {
  id: number;
  name: string;
  registrationNumber: string;
  hospitalTypeCode: string;
  hospitalTypeLabel?: string;
  stateId: number;
  stateName?: string;
  districtId: number;
  districtName?: string;
  cityId: number;
  cityName?: string;
  addressLine?: string;
  phone: string;
  email?: string;
  active: boolean;
  contacts?: HospitalContactResponse[];
}

export interface HospitalRequest {
  name: string;
  registrationNumber: string;
  hospitalTypeCode: string;
  stateId: number;
  districtId: number;
  cityId: number;
  addressLine?: string;
  phone: string;
  email?: string;
}

export const HospitalApi = {
  list: (params?: { page?: number; size?: number; isActive?: boolean }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    if (params?.isActive !== undefined) query.append('isActive', params.isActive.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<HospitalResponse>>('GET', `/hospitals${qs}`);
  },
  get: (id: number) => request<HospitalResponse>('GET', `/hospitals/${id}`),
  create: (body: HospitalRequest) => request<HospitalResponse>('POST', '/hospitals', body),
  update: (id: number, body: HospitalRequest) => request<HospitalResponse>('PUT', `/hospitals/${id}`, body),
  delete: (id: number) => request<void>('DELETE', `/hospitals/${id}`),
};
