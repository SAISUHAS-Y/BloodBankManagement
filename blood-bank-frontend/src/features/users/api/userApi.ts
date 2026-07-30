import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';

export interface UserSummaryResponse {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
  active: boolean;
}

export interface DonorBasicResponse {
  id: number;
  fullName: string;
  bloodGroupLabel?: string;
  gender?: string;
  city?: string;
  eligibleToDonate: boolean;
  totalDonationsCount: number;
}

export interface DonorProfileRequest {
  fullName: string;
  bloodGroupId: number;
  gender: string;
  dateOfBirth?: string;
  phone: string;
  email?: string;
  cityId?: number;
  districtId?: number;
  stateId?: number;
  addressLine?: string;
}

export interface StaffProfileResponse {
  id: number;
  identityUserId: number;
  fullName: string;
  employeeCode: string;
  designation: string;
  employmentType: string;
  hospitalId?: number;
  hospitalName?: string;
  bloodBankId?: number;
  bloodBankName?: string;
  status: string;
}

export interface StaffProfileRequest {
  identityUserId: number;
  fullName: string;
  employeeCode: string;
  designation: string;
  employmentType: string;
  hospitalId?: number;
  bloodBankId?: number;
}

export const UserApi = {
  list: (params?: { page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<UserSummaryResponse>>('GET', `/admin/users/search${qs}`);
  },
};

export const DonorApi = {
  list: (params?: { page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<DonorBasicResponse>>('GET', `/donors${qs}`);
  },
  get: (id: number) => request<DonorBasicResponse>('GET', `/donors/${id}`),
  create: (body: DonorProfileRequest) => request<DonorBasicResponse>('POST', '/donors', body),
  update: (id: number, body: DonorProfileRequest) => request<DonorBasicResponse>('PUT', `/donors/${id}`, body),
};

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
