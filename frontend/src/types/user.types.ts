import type { Role } from './role.types';

export interface User {
  id: number;
  email: string;
  password?: string;
  roleIds?: number[];
}

export interface UserWithRoles extends User {
  roles?: Role[];
}

