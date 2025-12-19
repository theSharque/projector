import apiClient from './client';
import type { Task } from '@/types/api.types';

export const taskApi = {
  getAll: async () => {
    const response = await apiClient.get<Task[]>('/api/tasks');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<Task>(`/api/tasks/${id}`);
    return response.data;
  },

  create: async (task: Task) => {
    const response = await apiClient.post<Task>('/api/tasks', task);
    return response.data;
  },

  update: async (id: number, task: Task) => {
    const response = await apiClient.put<Task>(`/api/tasks/${id}`, task);
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/api/tasks/${id}`);
  },
};

