import { request } from '../../../api/http';

export interface BloodGroupResponse {
  id: number;
  code: string;
  displayName: string;
  universalDonor: boolean;
  universalRecipient: boolean;
}

export interface LookupItemResponse {
  id: number;
  code: string;
  label: string;
  sortOrder: number;
  active: boolean;
}

export interface MasterDataBundleResponse {
  bloodGroups: BloodGroupResponse[];
  componentTypes: LookupItemResponse[];
  categories: Record<string, LookupItemResponse[]>;
}

export const MasterApi = {
  getBloodGroups: () => request<BloodGroupResponse[]>('GET', '/master/blood-groups'),
  getLookupItems: (categoryCode: string) => request<LookupItemResponse[]>('GET', `/master/lookups/${categoryCode}`),
  getBundle: () => request<MasterDataBundleResponse>('GET', '/master/bundle'),
};
