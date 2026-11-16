import { render, screen, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../shared/i18n';
import { LanguagesPage } from '../pages/LanguagesPage';
import { languagesApi } from '../api';

vi.mock('../api', () => ({
  languagesApi: {
    getLanguages: vi.fn(),
  },
}));

describe('LanguagesPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders published languages and draft languages', async () => {
    vi.mocked(languagesApi.getLanguages).mockResolvedValueOnce([
      {
        slug: 'java',
        name: 'Java',
        grammarId: 'java',
        indentStyle: 'SPACE',
        indentSize: 4,
        keywordCase: 'LOWER',
        release: 1,
        status: 'PUBLISHED',
        sortOrder: 1,
      },
      {
        slug: 'typescript',
        name: 'TypeScript',
        grammarId: 'typescript',
        indentStyle: 'SPACE',
        indentSize: 2,
        keywordCase: 'LOWER',
        release: 2,
        status: 'DRAFT',
        sortOrder: 6,
      },
    ]);

    render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>
          <LanguagesPage />
        </MemoryRouter>
      </I18nextProvider>
    );

    await waitFor(() => {
      expect(screen.getByText('Java')).toBeInTheDocument();
      expect(screen.getByText('TypeScript')).toBeInTheDocument();
      expect(screen.getByText(/Available in Release 1/i)).toBeInTheDocument();
      expect(screen.getByText(/Coming Soon/i)).toBeInTheDocument();
    });
  });
});
