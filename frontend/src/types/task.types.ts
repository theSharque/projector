import type { Feature } from './feature.types';
import type { User } from './user.types';

export interface Task {
  id: number;
  featureId: number;
  roadmapId: number;
  summary?: string;
  description?: string;
  createDate?: string;
  updateDate?: string;
  authorId: number;
}

export interface TaskWithFeature extends Task {
  feature?: Feature;
  author?: User;
}

