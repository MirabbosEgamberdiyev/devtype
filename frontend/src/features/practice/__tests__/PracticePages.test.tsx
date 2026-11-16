import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { PracticePage } from '../PracticePage';
import { SpeedTestPage } from '../SpeedTestPage';
import { SpecialCharsPage } from '../SpecialCharsPage';
import '../../../shared/i18n';

// Mock TypingView to focus on page orchestration
vi.mock('../../typing/TypingView', () => ({
  TypingView: (props: {
    language?: string;
    mode?: string;
    duration?: number;
    specialCategory?: string;
  }) => (
    <div data-testid="typing-view-mock">
      <span>Language: {props.language}</span>
      <span>Mode: {props.mode}</span>
      {props.duration && <span>Duration: {props.duration}</span>}
      {props.specialCategory && <span>Category: {props.specialCategory}</span>}
    </div>
  ),
}));

describe('Practice Pages Integration', () => {
  beforeEach(() => {
    vi.restoreAllMocks();
  });

  it('renders PracticePage with route parameters', () => {
    render(
      <MemoryRouter initialEntries={['/practice/python/basics']}>
        <Routes>
          <Route path="/practice/:lang/:topic" element={<PracticePage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByTestId('typing-view-mock')).toBeInTheDocument();
    expect(screen.getByText('Language: python')).toBeInTheDocument();
    expect(screen.getByText('Mode: PRACTICE')).toBeInTheDocument();
  });

  it('renders SpeedTestPage with duration selector', () => {
    render(
      <MemoryRouter initialEntries={['/speed-test/java']}>
        <Routes>
          <Route path="/speed-test/:lang" element={<SpeedTestPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/select duration|davomiylikni tanlang|выберите длительность/i)).toBeInTheDocument();
    expect(screen.getByText('30s')).toBeInTheDocument();
    expect(screen.getByText('60s')).toBeInTheDocument();
    expect(screen.getByText('120s')).toBeInTheDocument();
    expect(screen.getByText('Language: java')).toBeInTheDocument();
    expect(screen.getByText('Mode: SPEED_TEST')).toBeInTheDocument();
    expect(screen.getByText('Duration: 60')).toBeInTheDocument();

    // Click 30s
    fireEvent.click(screen.getByText('30s'));
    expect(screen.getByText('Duration: 30')).toBeInTheDocument();
  });

  it('renders SpecialCharsPage with category selector', () => {
    render(
      <MemoryRouter initialEntries={['/practice/javascript/special']}>
        <Routes>
          <Route path="/practice/:lang/special" element={<SpecialCharsPage />} />
        </Routes>
      </MemoryRouter>
    );

    expect(screen.getByText(/select symbol category|belgilar toifasini tanlang|выберите категорию/i)).toBeInTheDocument();
    expect(screen.getByText('BRACKETS')).toBeInTheDocument();
    expect(screen.getByText('ASSIGN_CMP')).toBeInTheDocument();
    expect(screen.getByText('Language: javascript')).toBeInTheDocument();
    expect(screen.getByText('Mode: SPECIAL_CHARS')).toBeInTheDocument();
    expect(screen.getByText('Category: BRACKETS')).toBeInTheDocument();

    // Click ASSIGN_CMP
    fireEvent.click(screen.getByText('ASSIGN_CMP'));
    expect(screen.getByText('Category: ASSIGN_CMP')).toBeInTheDocument();
  });
});
