import { describe, it, expect } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { CommandPalette } from '../CommandPalette';

describe('CommandPalette Component (AC-UX-7)', () => {
  it('opens on Ctrl+K and filters command items by query', () => {
    render(
      <MemoryRouter>
        <CommandPalette />
      </MemoryRouter>
    );

    // Initially closed
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();

    // Trigger Ctrl+K
    act(() => {
      window.dispatchEvent(
        new KeyboardEvent('keydown', {
          key: 'k',
          ctrlKey: true,
          bubbles: true,
        })
      );
    });

    // Dialog should now be open
    expect(screen.getByRole('dialog')).toBeInTheDocument();
    expect(screen.getByPlaceholderText(/Type a command/i)).toBeInTheDocument();

    // Filter by query "Analytics"
    const input = screen.getByPlaceholderText(/Type a command/i);
    fireEvent.change(input, { target: { value: 'Analytics' } });

    expect(screen.getByText('Analytics')).toBeInTheDocument();

    // Press Escape to close
    act(() => {
      window.dispatchEvent(
        new KeyboardEvent('keydown', {
          key: 'Escape',
          bubbles: true,
        })
      );
    });

    expect(screen.queryByRole('dialog')).not.toBeInTheDocument();
  });
});
