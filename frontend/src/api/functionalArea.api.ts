import apiClient from './client';
import type { FunctionalArea } from '@/types/api.types';

export const functionalAreaApi = {
  getAll: async () => {
    const response = await apiClient.get<FunctionalArea[]>('/api/functional-areas');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<FunctionalArea>(`/api/functional-areas/${id}`);
    return response.data;
  },

  getUsage: async (id: number) => {
    const response = await apiClient.get<number>(`/api/functional-areas/${id}/usage`);
    return response.data;
  },

  create: async (functionalArea: Omit<FunctionalArea, 'id' | 'createDate' | 'updateDate'>) => {
    const response = await apiClient.post<FunctionalArea>('/api/functional-areas', functionalArea);
    return response.data;
  },

  update: async (id: number, functionalArea: Omit<FunctionalArea, 'id' | 'createDate' | 'updateDate'>) => {
    const response = await apiClient.put<FunctionalArea>(`/api/functional-areas/${id}`, functionalArea);
    return response.data;
  },

  delete: async (id: number, replacementFaId: number) => {
    await apiClient.delete(`/api/functional-areas/${id}?replacementFaId=${replacementFaId}`);
  },
};
