import React, { useState } from 'react';
import { Outlet, NavLink, Link, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuthStore } from '../../shared/stores/authStore';
import { CommandPalette } from '../../features/command-palette/CommandPalette';

export const AppLayout: React.FC = () => {
  const { t } = useTranslation('common');
  const navigate = useNavigate();
  const { user, logout } = useAuthStore();
  const [collapsed, setCollapsed] = useState(false);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    { to: '/dashboard', label: t('nav.dashboard', 'Dashboard'), icon: '📊' },
    { to: '/practice', label: t('nav.practice', 'Practice'), icon: '⌨️' },
    { to: '/speed-test', label: t('nav.speedTest', 'Speed Test'), icon: '⚡' },
    { to: '/history', label: t('history.title', 'History'), icon: '📜' },
    { to: '/analytics', label: t('nav.analytics', 'Analytics'), icon: '📈' },
    { to: '/settings', label: t('nav.settings', 'Settings'), icon: '⚙️' },
  ];

  if (user?.role === 'ADMIN' || user?.role === 'EDITOR') {
    navItems.push({
      to: '/admin/exercises',
      label: 'Admin',
      icon: '🛡️',
    });
  }

  return (
    <div className="min-h-screen bg-bg text-text flex">
      {/* Sidebar (Desktop / Tablet) */}
      <aside
        className={`hidden md:flex flex-col border-r border-border bg-surface transition-all duration-200 select-none ${
          collapsed ? 'w-16' : 'w-60'
        }`}
      >
        {/* Brand */}
        <div className="h-14 border-b border-border px-4 flex items-center justify-between">
          {!collapsed && (
            <Link to="/dashboard" className="font-bold text-lg text-accent flex items-center gap-2">
              <span>⚡ DevType</span>
            </Link>
          )}
          <button
            onClick={() => setCollapsed(!collapsed)}
            aria-label="Toggle sidebar"
            className="p-1.5 rounded-lg hover:bg-surface-2 text-text-muted hover:text-text mx-auto"
          >
            {collapsed ? '▶' : '◀'}
          </button>
        </div>

        {/* Nav Links */}
        <nav className="flex-1 p-3 space-y-1">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-accent text-accent-fg font-semibold'
                    : 'text-text-muted hover:text-text hover:bg-surface-2'
                }`
              }
              title={collapsed ? item.label : undefined}
            >
              <span className="text-base">{item.icon}</span>
              {!collapsed && <span>{item.label}</span>}
            </NavLink>
          ))}
        </nav>

        {/* User Mini Profile */}
        <div className="p-3 border-t border-border">
          <Link
            to="/profile"
            className="flex items-center gap-3 p-2 rounded-xl hover:bg-surface-2 transition-colors"
            title={user?.username}
          >
            <div className="w-8 h-8 rounded-lg bg-accent/20 border border-accent/40 flex items-center justify-center text-xs font-bold font-mono text-accent">
              {user?.username?.substring(0, 2).toUpperCase() || 'U'}
            </div>
            {!collapsed && (
              <div className="flex-1 min-w-0">
                <div className="text-xs font-semibold text-text truncate">{user?.username}</div>
                <div className="text-[10px] text-text-muted truncate">{user?.email}</div>
              </div>
            )}
          </Link>
        </div>
      </aside>

      {/* Main Content Area */}
      <div className="flex-1 flex flex-col min-w-0">
        {/* Header (56px) */}
        <header className="h-14 border-b border-border bg-surface px-4 md:px-6 flex items-center justify-between">
          <div className="flex items-center gap-3">
            {/* Mobile Brand Link */}
            <Link to="/dashboard" className="md:hidden font-bold text-accent">
              ⚡ DevType
            </Link>

            {/* Command Palette Trigger Button */}
            <button
              onClick={() => {
                const event = new KeyboardEvent('keydown', {
                  key: 'k',
                  ctrlKey: true,
                  bubbles: true,
                });
                window.dispatchEvent(event);
              }}
              className="flex items-center gap-2 px-3 py-1.5 rounded-lg bg-surface-2 border border-border text-xs text-text-muted hover:border-border-strong transition-colors"
              aria-label="Open command palette"
            >
              <span>🔍</span>
              <span className="hidden sm:inline">{t('command.quickSearch', 'Quick search...')}</span>
              <kbd className="text-[10px] px-1.5 py-0.5 rounded bg-surface border border-border font-mono">
                Ctrl K
              </kbd>
            </button>
          </div>

          {/* Right Header items */}
          <div className="flex items-center gap-3">
            <button
              onClick={handleLogout}
              className="text-xs text-text-muted hover:text-red-400 px-2.5 py-1 rounded-lg border border-border/60 hover:border-red-400/40 transition-colors"
            >
              {t('nav.logout', 'Log out')}
            </button>
          </div>
        </header>

        {/* Page Content */}
        <main className="flex-1 p-4 md:p-8 max-w-7xl w-full mx-auto pb-20 md:pb-8">
          <Outlet />
        </main>

        {/* Bottom Navigation Bar for Mobile (xs / sm) */}
        <nav className="md:hidden fixed bottom-0 left-0 right-0 h-14 bg-surface border-t border-border flex items-center justify-around z-40 px-2">
          {navItems.slice(0, 5).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex flex-col items-center justify-center p-1 text-[10px] ${
                  isActive ? 'text-accent font-bold' : 'text-text-muted'
                }`
              }
            >
              <span className="text-base">{item.icon}</span>
              <span>{item.label}</span>
            </NavLink>
          ))}
        </nav>
      </div>

      {/* Global Command Palette Dialog */}
      <CommandPalette />
    </div>
  );
};
