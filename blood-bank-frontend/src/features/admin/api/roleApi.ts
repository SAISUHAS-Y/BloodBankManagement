import { request } from '../../../api/http';

export interface RoleResponse {
  id: number;
  code: string;
  name: string;
  description?: string;
  enabled: boolean;
  permissions?: string[];
}

export interface CreateRoleRequest {
  code: string;
  name: string;
  description?: string;
}

export const RoleApi = {
  list: () => request<RoleResponse[]>('GET', '/admin/roles'),
  get: (id: number) => request<RoleResponse>('GET', `/admin/roles/${id}`),
  create: (body: CreateRoleRequest) => request<RoleResponse>('POST', '/admin/roles', body),
  enable: (id: number) => request<void>('POST', `/admin/roles/${id}/enable`),
  disable: (id: number) => request<void>('POST', `/admin/roles/${id}/disable`),
  delete: (id: number) => request<void>('DELETE', `/admin/roles/${id}`),
};
