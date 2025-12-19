import { create } from 'zustand';

interface AuthState {
  isAuthenticated: boolean;
  authorities: Set<string>;
  setAuthenticated: (authorities: Set<string>) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  isAuthenticated: false,
  authorities: new Set(),
  setAuthenticated: (authorities) => set({ isAuthenticated: true, authorities }),
  logout: () => set({ isAuthenticated: false, authorities: new Set() }),
}));

