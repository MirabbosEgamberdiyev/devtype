import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { SupportedLocale, SUPPORTED_LOCALES, LOCALE_LABELS } from '../../shared/i18n';

interface CommandItem {
  id: string;
  title: string;
  category: string;
  icon: string;
  action: () => void;
}

export const CommandPalette: React.FC = () => {
  const { t, i18n } = useTranslation('common');
  const navigate = useNavigate();
  const [isOpen, setIsOpen] = useState(false);
  const [query, setQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);

  const inputRef = useRef<HTMLInputElement>(null);

  // Global Ctrl+K / Cmd+K listener
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.ctrlKey || e.metaKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setIsOpen((prev) => !prev);
        setQuery('');
        setSelectedIndex(0);
      } else if (e.key === 'Escape' && isOpen) {
        e.preventDefault();
        setIsOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, [isOpen]);

  useEffect(() => {
    if (isOpen) {
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }, [isOpen]);

  // Command items list
  const commands: CommandItem[] = [
    // Navigation
    {
      id: 'nav-dashboard',
      title: t('nav.dashboard', 'Dashboard'),
      category: t('command.navigation', 'Navigation'),
      icon: '📊',
      action: () => navigate('/dashboard'),
    },
    {
      id: 'nav-practice',
      title: t('nav.practice', 'Practice'),
      category: t('command.navigation', 'Navigation'),
      icon: '⌨️',
      action: () => navigate('/practice'),
    },
    {
      id: 'nav-speed-test',
      title: t('nav.speedTest', 'Speed Test'),
      category: t('command.navigation', 'Navigation'),
      icon: '⚡',
      action: () => navigate('/speed-test'),
    },
    {
      id: 'nav-history',
      title: t('history.title', 'Session History'),
      category: t('command.navigation', 'Navigation'),
      icon: '📜',
      action: () => navigate('/history'),
    },
    {
      id: 'nav-analytics',
      title: t('nav.analytics', 'Analytics'),
      category: t('command.navigation', 'Navigation'),
      icon: '📈',
      action: () => navigate('/analytics'),
    },
    {
      id: 'nav-profile',
      title: t('nav.profile', 'Profile'),
      category: t('command.navigation', 'Navigation'),
      icon: '👤',
      action: () => navigate('/profile'),
    },
    {
      id: 'nav-settings',
      title: t('nav.settings', 'Settings'),
      category: t('command.navigation', 'Navigation'),
      icon: '⚙️',
      action: () => navigate('/settings'),
    },
    {
      id: 'nav-languages',
      title: t('dashboard.myLanguages', 'Languages'),
      category: t('command.navigation', 'Navigation'),
      icon: '🌐',
      action: () => navigate('/languages'),
    },

    // Modes & Drills
    {
      id: 'mode-special-sql',
      title: 'Special Characters: SQL',
      category: t('command.drills', 'Drills'),
      icon: '🔣',
      action: () => navigate('/practice/sql/special'),
    },
    {
      id: 'mode-special-plsql',
      title: 'Special Characters: PL/SQL',
      category: t('command.drills', 'Drills'),
      icon: '🔣',
      action: () => navigate('/practice/plsql/special'),
    },

    // Quick Actions
    {
      id: 'act-toggle-theme',
      title: t('command.toggleTheme', 'Toggle Theme (Dark / Light)'),
      category: t('command.actions', 'Quick Actions'),
      icon: '🌓',
      action: () => {
        const cur = document.documentElement.getAttribute('data-theme') || 'dark';
        const next = cur === 'dark' ? 'light' : 'dark';
        document.documentElement.setAttribute('data-theme', next);
        localStorage.setItem('dt_theme', next);
      },
    },
    ...SUPPORTED_LOCALES.map((loc) => ({
      id: `act-locale-${loc}`,
      title: `${t('command.switchLanguage', 'Switch Language')}: ${LOCALE_LABELS[loc]}`,
      category: t('command.actions', 'Quick Actions'),
      icon: '🌐',
      action: () => {
        i18n.changeLanguage(loc as SupportedLocale);
        localStorage.setItem('dt_locale', loc);
        document.documentElement.setAttribute('lang', loc);
      },
    })),
  ];

  const filtered = commands.filter((cmd) =>
    `${cmd.title} ${cmd.category}`.toLowerCase().includes(query.toLowerCase())
  );

  const handleSelect = (cmd: CommandItem) => {
    setIsOpen(false);
    cmd.action();
  };

  const handleKeyDown = (e: React.KeyboardEvent) => {
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev + 1) % Math.max(1, filtered.length));
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setSelectedIndex((prev) => (prev - 1 + filtered.length) % Math.max(1, filtered.length));
    } else if (e.key === 'Enter') {
      e.preventDefault();
      if (filtered[selectedIndex]) {
        handleSelect(filtered[selectedIndex]);
      }
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black/60 backdrop-blur-xs z-50 flex items-start justify-center pt-20 p-4"
      onClick={() => setIsOpen(false)}
      role="dialog"
      aria-modal="true"
      aria-label="Command Palette"
    >
      <div
        className="w-full max-w-xl rounded-2xl border border-border bg-surface shadow-2xl overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Search Input Bar */}
        <div className="p-3 border-b border-border flex items-center gap-3 bg-surface-2">
          <span className="text-text-muted text-sm">🔍</span>
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => {
              setQuery(e.target.value);
              setSelectedIndex(0);
            }}
            onKeyDown={handleKeyDown}
            placeholder={t(
              'command.placeholder',
              'Type a command, page name, or quick action...'
            )}
            className="flex-1 bg-transparent text-text text-sm outline-none placeholder:text-text-muted"
          />
          <kbd className="text-[10px] px-1.5 py-0.5 rounded bg-surface border border-border text-text-muted font-mono">
            ESC
          </kbd>
        </div>

        {/* Filtered List */}
        <div className="max-h-80 overflow-y-auto p-2 divide-y divide-border/40">
          {filtered.length === 0 ? (
            <div className="p-6 text-center text-xs text-text-muted">
              {t('command.noResults', 'No matching commands found')}
            </div>
          ) : (
            filtered.map((item, idx) => {
              const isSelected = idx === selectedIndex;
              return (
                <div
                  key={item.id}
                  onClick={() => handleSelect(item)}
                  onMouseEnter={() => setSelectedIndex(idx)}
                  className={`p-2.5 rounded-xl cursor-pointer flex items-center justify-between text-sm transition-colors ${
                    isSelected
                      ? 'bg-accent text-accent-fg font-medium'
                      : 'text-text hover:bg-surface-2'
                  }`}
                >
                  <div className="flex items-center gap-3">
                    <span className="text-base">{item.icon}</span>
                    <span>{item.title}</span>
                  </div>
                  <span
                    className={`text-[10px] px-2 py-0.5 rounded font-mono ${
                      isSelected ? 'bg-accent-fg/20 text-accent-fg' : 'text-text-muted bg-surface-2'
                    }`}
                  >
                    {item.category}
                  </span>
                </div>
              );
            })
          )}
        </div>
      </div>
    </div>
  );
};
