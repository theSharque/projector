import apiClient from './client';
import type { User } from '@/types/api.types';

export const userApi = {
  getAll: async () => {
    const response = await apiClient.get<User[]>('/api/users');
    return response.data;
  },

  getById: async (id: number) => {
    const response = await apiClient.get<User>(`/api/users/${id}`);
    return response.data;
  },

  getByEmail: async (email: string) => {
    const response = await apiClient.get<User>(`/api/users/email/${email}`);
    return response.data;
  },

  create: async (user: User) => {
    const response = await apiClient.post<User>('/api/users', user);
    return response.data;
  },

  update: async (id: number, user: User) => {
    const response = await apiClient.put<User>(`/api/users/${id}`, user);
    return response.data;
  },

  delete: async (id: number) => {
    await apiClient.delete(`/api/users/${id}`);
  },
};

