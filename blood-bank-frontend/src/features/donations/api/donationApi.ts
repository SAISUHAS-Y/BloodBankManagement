import { request } from '../../../api/http';
import { PageResponse } from '../../inventory/api/facilityApi';

export interface DonationRecordResponse {
  id: number;
  donorProfileId: number;
  donorName?: string;
  bloodBankId: number;
  bloodBankName?: string;
  bloodGroupId: number;
  bloodGroupLabel?: string;
  unitsDonated: number;
  status: 'IN_PROGRESS' | 'COMPLETED' | 'REJECTED' | 'DISCARDED';
  donationDate?: string;
  donatedAt?: string;
  notes?: string;
}

export interface DonationRecordRequest {
  donorProfileId: number;
  bloodBankId: number;
  bloodGroupId: number;
  unitsDonated: number;
  notes?: string;
}

export interface EligibilityRequest {
  donorProfileId: number;
  weightKg: number;
  hemoglobinGdl: number;
  pulseBpm: number;
  systolicBp: number;
  diastolicBp: number;
  temperatureCelsius: number;
}

export interface EligibilityResponse {
  eligible: boolean;
  rejectionReasons: string[];
  nextEligibleDate?: string;
}

export const DonationApi = {
  list: (params?: { donorProfileId?: number; bloodBankId?: number; status?: string; page?: number; size?: number }) => {
    const query = new URLSearchParams();
    if (params?.donorProfileId) query.append('donorProfileId', params.donorProfileId.toString());
    if (params?.bloodBankId) query.append('bloodBankId', params.bloodBankId.toString());
    if (params?.status) query.append('status', params.status);
    if (params?.page !== undefined) query.append('page', params.page.toString());
    if (params?.size !== undefined) query.append('size', params.size.toString());
    const qs = query.toString() ? `?${query.toString()}` : '';
    return request<PageResponse<DonationRecordResponse>>('GET', `/donations${qs}`);
  },
  get: (id: number) => request<DonationRecordResponse>('GET', `/donations/${id}`),
  create: (body: DonationRecordRequest) => request<DonationRecordResponse>('POST', '/donations', body),
  complete: (id: number) => request<DonationRecordResponse>('PATCH', `/donations/${id}/complete`),
  checkEligibility: (body: EligibilityRequest, componentTypeCode: string = 'WHOLE_BLOOD') =>
    request<EligibilityResponse>('POST', `/donations/eligibility?componentTypeCode=${componentTypeCode}`, body),
};
