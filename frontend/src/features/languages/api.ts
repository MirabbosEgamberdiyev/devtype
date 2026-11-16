import { apiClient } from '../../shared/api/client';

export interface LanguageResponse {
  slug: string;
  name: string;
  grammarId: string;
  indentStyle: string;
  indentSize: number;
  keywordCase: string;
  release: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
  sortOrder: number;
}

export interface TopicResponse {
  slug: string;
  name: string;
  description: string;
  sortOrder: number;
  status: 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
}

export const languagesApi = {
  getLanguages: (): Promise<LanguageResponse[]> =>
    apiClient<LanguageResponse[]>('/languages'),

  getLanguageTopics: (slug: string): Promise<TopicResponse[]> =>
    apiClient<TopicResponse[]>(`/languages/${slug}/topics`),
};
