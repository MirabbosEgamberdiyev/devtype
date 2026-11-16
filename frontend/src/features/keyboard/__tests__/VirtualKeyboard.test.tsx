import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { VirtualKeyboard } from '../VirtualKeyboard';

describe('VirtualKeyboard (AC-VK-1..8)', () => {
  it('renders ANSI-US keyboard layout with pointer-events-none', () => {
    render(<VirtualKeyboard />);
    const keyboard = screen.getByTestId('virtual-keyboard');
    expect(keyboard).toBeInTheDocument();
    expect(keyboard).toHaveClass('pointer-events-none');
    expect(screen.getByText('Space')).toBeInTheDocument();
    expect(screen.getByText('Enter')).toBeInTheDocument();
  });

  it('highlights target key for standard lowercase char (AC-VK-3)', () => {
    const { container } = render(<VirtualKeyboard nextChar="a" />);
    // KeyA should have ring-1 ring-accent-primary animate-pulse
    const targetKey = container.querySelector('.ring-accent-primary');
    expect(targetKey).toBeInTheDocument();
    expect(targetKey?.textContent).toContain('a');
  });

  it('highlights target key AND opposite Shift for uppercase char (AC-VK-4)', () => {
    // 'A' is on left hand (finger 0), so it should require right shift (ShiftRight)
    const { container } = render(<VirtualKeyboard nextChar="A" />);
    const highlightedKeys = container.querySelectorAll('.ring-accent-primary');
    expect(highlightedKeys.length).toBe(2);

    const keyTexts = Array.from(highlightedKeys).map((k) => k.textContent);
    expect(keyTexts.some((t) => t?.includes('A') || t?.includes('a'))).toBe(true);
    expect(keyTexts.some((t) => t?.includes('Shift'))).toBe(true);
  });

  it('highlights left shift for right hand char (AC-VK-4)', () => {
    // 'P' is on right hand (finger 7), so it should require left shift (ShiftLeft)
    const { container } = render(<VirtualKeyboard nextChar="P" />);
    const highlightedKeys = container.querySelectorAll('.ring-accent-primary');
    expect(highlightedKeys.length).toBe(2);
  });

  it('highlights pressed key when lastPressedCode is provided (AC-VK-5)', () => {
    const { container } = render(<VirtualKeyboard lastPressedCode="KeyA" />);
    const pressedKey = container.querySelector('.bg-accent-primary.text-white');
    expect(pressedKey).toBeInTheDocument();
    expect(pressedKey?.textContent).toContain('a');
  });

  it('renders finger zone indicators (AC-VK-2)', () => {
    const { container } = render(<VirtualKeyboard showFingers={true} />);
    const fingerKeys = container.querySelectorAll('[class*="border-b-2"]');
    expect(fingerKeys.length).toBeGreaterThan(30);
  });
});
