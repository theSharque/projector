import apiClient from './client';
import type { Feature } from '@/types/api.types';

export const featureApi = {
  getAll: async () => {
    const response = await apiClient.get<Feature[]>('/api/features');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<Feature>(`/api/features/${id}`);
    return response.data;
  },

  create: async (feature: Feature) => {
    const response = await apiClient.post<Feature>('/api/features', feature);
    return response.data;
  },

  update: async (id: number, feature: Feature) => {
    const response = await apiClient.put<Feature>(`/api/features/${id}`, feature);
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/api/features/${id}`);
  },
};

