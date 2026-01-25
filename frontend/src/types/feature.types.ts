import type { User } from './user.types';
import type { Task } from './task.types';
import type { FunctionalArea } from './functionalArea.types';

export type Quarter = 'Q1' | 'Q2' | 'Q3' | 'Q4';

export interface Feature {
  id: number;
  year: number;
  quarter: Quarter;
  createDate?: string;
  updateDate?: string;
  authorId: number;
  sprint?: number;
  release?: string;
  summary?: string;
  description?: string;
  functionalAreaIds?: number[];
}

export interface FeatureWithTasks extends Feature {
  author?: User;
  tasks?: Task[];
  functionalAreas?: FunctionalArea[];
}

