import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { MemoryRouter } from 'react-router-dom';
import { I18nextProvider } from 'react-i18next';
import i18n from '../../../shared/i18n';
import { SnippetEditorPage } from '../pages/SnippetEditorPage';
import { apiClient } from '../../../shared/api/client';

vi.mock('../../../shared/api/client', () => ({
  apiClient: vi.fn(),
}));

describe('SnippetEditorPage Component', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  const renderWithProviders = () => {
    return render(
      <I18nextProvider i18n={i18n}>
        <MemoryRouter>
          <SnippetEditorPage />
        </MemoryRouter>
      </I18nextProvider>
    );
  };

  it('renders editor form fields properly', () => {
    renderWithProviders();
    expect(screen.getByLabelText(/Language/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Difficulty/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Snippet Title/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Source Code/i)).toBeInTheDocument();
  });

  it('detects lines exceeding 80 columns and disables submission', () => {
    renderWithProviders();

    const codeArea = screen.getByLabelText(/Source Code/i);
    const longLine = '// ' + 'X'.repeat(85);
    fireEvent.change(codeArea, { target: { value: longLine } });

    expect(screen.getByText(/1 line\(s\) > 80 cols/i)).toBeInTheDocument();
    const submitBtn = screen.getByRole('button', { name: /Save Exercise/i });
    expect(submitBtn).toBeDisabled();
  });

  it('detects forbidden syntax in PL/SQL snippets', () => {
    renderWithProviders();

    fireEvent.change(screen.getByLabelText(/Language/i), { target: { value: 'plsql' } });
    const codeArea = screen.getByLabelText(/Source Code/i);
    fireEvent.change(codeArea, { target: { value: 'CREATE FUNCTION foo() RETURNS void AS $$ BEGIN null; END; $$' } });

    expect(screen.getByText(/Disallowed syntax detected/i)).toBeInTheDocument();
    const submitBtn = screen.getByRole('button', { name: /Save Exercise/i });
    expect(submitBtn).toBeDisabled();
  });

  it('successfully creates valid snippet', async () => {
    vi.mocked(apiClient).mockResolvedValueOnce({
      exerciseKey: 'ex_uuid_123',
      status: 'DRAFT',
    });

    renderWithProviders();

    fireEvent.change(screen.getByLabelText(/Snippet Title/i), { target: { value: 'Valid Java Snippet' } });
    const validCode = 'public class Sample {\n    int value = 42;\n}';
    fireEvent.change(screen.getByLabelText(/Source Code/i), { target: { value: validCode } });

    const submitBtn = screen.getByRole('button', { name: /Save Exercise/i });
    expect(submitBtn).not.toBeDisabled();
    fireEvent.click(submitBtn);

    await waitFor(() => {
      expect(screen.getByRole('status')).toBeInTheDocument();
      expect(screen.getByText(/Exercise created successfully/i)).toBeInTheDocument();
    });
  });
});
