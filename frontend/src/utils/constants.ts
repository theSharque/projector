export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export const QUARTERS = ['Q1', 'Q2', 'Q3', 'Q4'] as const;

export const AUTHORITIES = {
  USER_VIEW: 'USER_VIEW',
  USER_EDIT: 'USER_EDIT',
  ROLE_VIEW: 'ROLE_VIEW',
  ROLE_EDIT: 'ROLE_EDIT',
  ROADMAP_VIEW: 'ROADMAP_VIEW',
  ROADMAP_EDIT: 'ROADMAP_EDIT',
  FEATURE_VIEW: 'FEATURE_VIEW',
  FEATURE_EDIT: 'FEATURE_EDIT',
  TASK_VIEW: 'TASK_VIEW',
  TASK_EDIT: 'TASK_EDIT',
} as const;

