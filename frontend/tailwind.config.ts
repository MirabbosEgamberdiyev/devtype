import type { Config } from 'tailwindcss';

export default {
  content: ['./index.html', './src/**/*.{js,ts,jsx,tsx}'],
  darkMode: ['selector', '[data-theme="dark"]'],
  theme: {
    extend: {
      colors: {
        bg: 'var(--bg)',
        surface: 'var(--surface)',
        'surface-2': 'var(--surface-2)',
        border: 'var(--border)',
        'border-strong': 'var(--border-strong)',
        text: 'var(--text)',
        'text-muted': 'var(--text-muted)',
        'text-faint': 'var(--text-faint)',
        accent: 'var(--accent)',
        'accent-fg': 'var(--accent-fg)',
        error: 'var(--error)',
        warning: 'var(--warning)',
        info: 'var(--info)',
        'focus-ring': 'var(--focus-ring)',
        rank: {
          bronze: 'var(--rank-bronze)',
          silver: 'var(--rank-silver)',
          gold: 'var(--rank-gold)',
          platinum: 'var(--rank-platinum)',
          diamond: 'var(--rank-diamond)',
        },
      },
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      maxWidth: {
        typing: '80ch',
      },
      spacing: {
        18: '4.5rem',
      },
      zIndex: {
        sidebar: '20',
        header: '30',
        dropdown: '40',
        modal: '50',
        toast: '60',
      },
    },
  },
  plugins: [],
} satisfies Config;
