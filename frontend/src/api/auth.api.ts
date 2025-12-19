import apiClient from './client';
import type { LoginRequest } from '@/types/api.types';

export const authApi = {
  login: async (credentials: LoginRequest) => {
    const response = await apiClient.post('/api/auth/login', credentials, {
      validateStatus: (status) => status === 204 || status === 401,
    });
    return response;
  },

  getProfile: async () => {
    const response = await apiClient.get<Set<string>>('/api/auth/profile');
    return response.data;
  },
};

