import type { User } from './user.types';

export interface Roadmap {
  id: number;
  projectName: string;
  createDate?: string;
  updateDate?: string;
  authorId: number;
  mission?: string;
  description?: string;
  participantIds?: number[];
}

export interface RoadmapWithParticipants extends Roadmap {
  author?: User;
  participants?: User[];
}

