import { request } from '../../../api/http';

export interface BloodStockResponse {
  id: number;
  bloodBankId: number;
  bloodBankName?: string;
  bloodGroupId: number;
  bloodGroupLabel: string;
  componentTypeCode?: string;
  componentTypeLabel?: string;
  unitsAvailable: number;
  minThresholdUnits?: number;
  lastUpdatedAt?: string;
}

export interface StockAdjustmentRequest {
  units: number;
  reason: string;
}

export interface BloodStockSearchRequest {
  bloodGroupCode?: string;
  stateId?: number;
  districtId?: number;
  minUnits?: number;
}

export interface StockSearchResponse {
  bloodBankId: number;
  bloodBankName: string;
  cityName: string;
  bloodGroupLabel: string;
  unitsAvailable: number;
}

export const StockApi = {
  getByBloodBank: (bloodBankId: number) => request<BloodStockResponse[]>('GET', `/blood-stocks/blood-bank/${bloodBankId}`),
  adjustStock: (id: number, body: StockAdjustmentRequest) => request<BloodStockResponse>('PATCH', `/blood-stocks/${id}/adjust`, body),
  searchStock: (body: BloodStockSearchRequest) => request<StockSearchResponse[]>('POST', '/blood-stocks/search', body),
};
