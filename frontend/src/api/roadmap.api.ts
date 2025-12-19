import apiClient from './client';
import type { Roadmap } from '@/types/api.types';

export const roadmapApi = {
  getAll: async () => {
    const response = await apiClient.get<Roadmap[]>('/api/roadmaps');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<Roadmap>(`/api/roadmaps/${id}`);
    return response.data;
  },

  create: async (roadmap: Roadmap) => {
    const response = await apiClient.post<Roadmap>('/api/roadmaps', roadmap);
    return response.data;
  },

  update: async (id: number, roadmap: Roadmap) => {
    const response = await apiClient.put<Roadmap>(`/api/roadmaps/${id}`, roadmap);
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/api/roadmaps/${id}`);
  },
};

