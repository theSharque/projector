import apiClient from './client';
import type { Role } from '@/types/api.types';

export const roleApi = {
  getAll: async () => {
    const response = await apiClient.get<Role[]>('/api/roles');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<Role>(`/api/roles/${id}`);
    return response.data;
  },

  create: async (role: Role) => {
    const response = await apiClient.post<Role>('/api/roles', role);
    return response.data;
  },

  update: async (id: number, role: Role) => {
    const response = await apiClient.put<Role>(`/api/roles/${id}`, role);
    return response.data;
  },

  updateAuthorities: async (id: number, authorities: Set<string>) => {
    const response = await apiClient.post<Role>(`/api/roles/${id}/authorities`, Array.from(authorities));
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/api/roles/${id}`);
  },
};

