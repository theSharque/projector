import type { User } from './user.types';
import type { Role } from './role.types';
import type { Roadmap } from './roadmap.types';
import type { Feature, Quarter } from './feature.types';
import type { Task } from './task.types';
import type { FunctionalArea, FunctionalAreaUsage } from './functionalArea.types';

export interface ApiResponse<T> {
  data: T;
}

export interface ApiError {
  message: string;
  status: number;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  user: User;
}

export type {
  User,
  Role,
  Roadmap,
  Feature,
  Task,
  Quarter,
  FunctionalArea,
  FunctionalAreaUsage,
};

