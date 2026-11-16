import React, { useEffect, useState, useRef } from 'react';
import { useTranslation } from 'react-i18next';
import { devtypeApi, UserSettings } from '../../shared/api/devtypeApi';
import { SupportedLocale, SUPPORTED_LOCALES, LOCALE_LABELS } from '../../shared/i18n';

type SettingsSection =
  | 'appearance'
  | 'typing'
  | 'editor'
  | 'keyboard'
  | 'account'
  | 'privacy'
  | 'danger';

const DEFAULT_SETTINGS: UserSettings = {
  theme: 'dark',
  locale: 'uzl',
  strictIndent: false,
  practiceStopOnError: true,
  showLiveStats: false,
  keySound: 'off',
  soundVolume: 50,
  blindMode: false,
  fontFamily: 'JetBrains Mono',
  fontSize: 18,
  lineHeight: 1.6,
  caretStyle: 'line',
  smoothCaret: true,
  ligatures: false,
  lineNumbers: true,
  showInvisibles: true,
  bracketMatching: true,
  layout: 'ansi-us',
  showVirtualKeyboard: 'auto',
  fingerGuide: true,
  profilePublic: true,
  showOnLeaderboard: true,
  coachEnabled: true,
  emailStreakReminder: false,
  emailWeeklyReport: false,
  emailAchievements: false,
  emailCompetitions: false,
};

export const SettingsPage: React.FC = () => {
  const { t, i18n } = useTranslation('common');
  const [activeSection, setActiveSection] = useState<SettingsSection>('appearance');
  const [settings, setSettings] = useState<UserSettings>(DEFAULT_SETTINGS);
  const [loading, setLoading] = useState(true);
  const [saveStatus, setSaveStatus] = useState<'idle' | 'saving' | 'saved' | 'error'>('idle');

  // Account details
  const [username, setUsername] = useState('');
  const [email, setEmail] = useState('');
  const [accountMsg, setAccountMsg] = useState<string | null>(null);

  // Deletion modal
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteStatus, setDeleteStatus] = useState<string | null>(null);

  const debounceTimer = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    Promise.all([devtypeApi.getUserSettings(), devtypeApi.getMe()])
      .then(([s, user]) => {
        setSettings({ ...DEFAULT_SETTINGS, ...s });
        setUsername(user.username);
        setEmail(user.email);
        setLoading(false);
      })
      .catch(() => {
        setLoading(false);
      });
  }, []);

  const updateSetting = <K extends keyof UserSettings>(key: K, value: UserSettings[K]) => {
    const updated = { ...settings, [key]: value };
    setSettings(updated);
    setSaveStatus('saving');

    if (key === 'theme') {
      document.documentElement.setAttribute('data-theme', value as string);
      localStorage.setItem('dt_theme', value as string);
    }
    if (key === 'locale') {
      const loc = value as SupportedLocale;
      i18n.changeLanguage(loc);
      localStorage.setItem('dt_locale', loc);
      document.documentElement.setAttribute('lang', loc);
    }

    if (debounceTimer.current) clearTimeout(debounceTimer.current);
    debounceTimer.current = setTimeout(() => {
      devtypeApi
        .updateUserSettings(updated)
        .then(() => {
          setSaveStatus('saved');
          setTimeout(() => setSaveStatus('idle'), 2000);
        })
        .catch(() => {
          setSaveStatus('error');
        });
    }, 500);
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setAccountMsg(null);
      await devtypeApi.updateProfile({ username });
      setAccountMsg(t('settings.profileUpdated', 'Profile updated successfully'));
    } catch (err: unknown) {
      const e = err as { detail?: string; message?: string };
      setAccountMsg(e.detail || e.message || 'Failed to update username');
    }
  };

  const handleExportData = async () => {
    try {
      const data = await devtypeApi.exportUserData();
      const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });
      const url = URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = `devtype-export-${new Date().toISOString().slice(0, 10)}.json`;
      a.click();
      URL.revokeObjectURL(url);
    } catch (err: unknown) {
      const e = err as Error;
      alert(e.message || 'Failed to export data');
    }
  };

  const handleDeleteAccount = async () => {
    try {
      const res = await devtypeApi.deleteAccount();
      setDeleteStatus(
        res.message ||
          'Account scheduled for deletion in 30 days. You have been logged out.'
      );
      setTimeout(() => {
        window.location.href = '/';
      }, 3000);
    } catch (err: unknown) {
      const e = err as Error;
      alert(e.message || 'Failed to delete account');
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] text-text-muted">
        <div className="w-8 h-8 border-2 border-accent border-t-transparent rounded-full animate-spin mb-3" />
        <p>{t('typing.loading', 'Loading settings...')}</p>
      </div>
    );
  }

  const sections: Array<{ id: SettingsSection; label: string; icon: string }> = [
    { id: 'appearance', label: t('settings.appearance', 'Appearance'), icon: '🎨' },
    { id: 'typing', label: t('settings.typing', 'Typing Rules'), icon: '⌨️' },
    { id: 'editor', label: t('settings.editor', 'Code Editor'), icon: '📝' },
    { id: 'keyboard', label: t('settings.keyboard', 'Keyboard & Layout'), icon: '🖐️' },
    { id: 'account', label: t('settings.account', 'Account'), icon: '👤' },
    { id: 'privacy', label: t('settings.privacy', 'Privacy & Export'), icon: '🔒' },
    { id: 'danger', label: t('settings.dangerZone', 'Danger Zone'), icon: '⚠️' },
  ];

  return (
    <div className="max-w-5xl mx-auto space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-2xl font-bold text-text">{t('settings.title', 'Settings')}</h2>
          <p className="text-sm text-text-muted mt-1">
            {t('settings.subtitle', 'Configure your training preferences, editor optics, and account parameters.')}
          </p>
        </div>
        <div className="text-xs font-mono">
          {saveStatus === 'saving' && <span className="text-amber-400">Saving...</span>}
          {saveStatus === 'saved' && <span className="text-emerald-400">✓ Saved</span>}
          {saveStatus === 'error' && <span className="text-red-400">Error saving</span>}
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        {/* Navigation Tabs */}
        <div className="space-y-1">
          {sections.map((s) => (
            <button
              key={s.id}
              onClick={() => setActiveSection(s.id)}
              className={`w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-colors flex items-center gap-2.5 ${
                activeSection === s.id
                  ? 'bg-accent text-accent-fg font-semibold'
                  : 'text-text-muted hover:text-text hover:bg-surface-2'
              }`}
            >
              <span>{s.icon}</span>
              <span>{s.label}</span>
            </button>
          ))}
        </div>

        {/* Settings Panel */}
        <div className="md:col-span-3 p-6 rounded-2xl border border-border bg-surface space-y-6">
          {/* Appearance Section */}
          {activeSection === 'appearance' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.appearance', 'Appearance')}
              </h3>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">{t('settings.theme', 'Theme')}</div>
                  <div className="text-xs text-text-muted">
                    {t('settings.themeDesc', 'Select dark or light interface theme')}
                  </div>
                </div>
                <select
                  value={settings.theme}
                  onChange={(e) => updateSetting('theme', e.target.value)}
                  className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                >
                  <option value="dark">Dark</option>
                  <option value="light">Light</option>
                </select>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">{t('settings.locale', 'Interface Language')}</div>
                  <div className="text-xs text-text-muted">
                    {t('settings.localeDesc', 'Choose your preferred language for the UI')}
                  </div>
                </div>
                <select
                  value={settings.locale}
                  onChange={(e) => updateSetting('locale', e.target.value)}
                  className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                >
                  {SUPPORTED_LOCALES.map((loc) => (
                    <option key={loc} value={loc}>
                      {LOCALE_LABELS[loc]}
                    </option>
                  ))}
                </select>
              </div>
            </div>
          )}

          {/* Typing Rules Section */}
          {activeSection === 'typing' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.typing', 'Typing Rules')}
              </h3>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.stopOnError', 'Stop on Error')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.stopOnErrorDesc', 'Enforces immediate error correction before advancing')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.practiceStopOnError}
                  onChange={(e) => updateSetting('practiceStopOnError', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.strictIndent', 'Strict Indentation')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.strictIndentDesc', 'Type every space or tab indentation character manually')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.strictIndent}
                  onChange={(e) => updateSetting('strictIndent', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.liveStats', 'Show Live WPM / Accuracy')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.liveStatsDesc', 'Displays live stats during session (hidden by default for focus)')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.showLiveStats}
                  onChange={(e) => updateSetting('showLiveStats', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">{t('settings.keySound', 'Key Sound')}</div>
                  <div className="text-xs text-text-muted">
                    {t('settings.keySoundDesc', 'Auditory feedback clicks')}
                  </div>
                </div>
                <select
                  value={settings.keySound}
                  onChange={(e) => updateSetting('keySound', e.target.value)}
                  className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                >
                  <option value="off">Off</option>
                  <option value="click">Mechanical Click</option>
                  <option value="typewriter">Typewriter</option>
                </select>
              </div>
            </div>
          )}

          {/* Editor Section with Preview */}
          {activeSection === 'editor' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.editor', 'Code Editor')}
              </h3>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-semibold text-text-muted block mb-1">
                    {t('settings.fontFamily', 'Font Family')}
                  </label>
                  <select
                    value={settings.fontFamily}
                    onChange={(e) => updateSetting('fontFamily', e.target.value)}
                    className="w-full bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                  >
                    <option value="JetBrains Mono">JetBrains Mono</option>
                    <option value="Fira Code">Fira Code</option>
                    <option value="monospace">Monospace</option>
                  </select>
                </div>

                <div>
                  <label className="text-xs font-semibold text-text-muted block mb-1">
                    {t('settings.fontSize', 'Font Size (px)')}
                  </label>
                  <input
                    type="number"
                    min={14}
                    max={24}
                    value={settings.fontSize}
                    onChange={(e) => updateSetting('fontSize', Number(e.target.value))}
                    className="w-full bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-xs font-semibold text-text-muted block mb-1">
                    {t('settings.caretStyle', 'Caret Style')}
                  </label>
                  <select
                    value={settings.caretStyle}
                    onChange={(e) => updateSetting('caretStyle', e.target.value)}
                    className="w-full bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                  >
                    <option value="line">Line (|)</option>
                    <option value="block">Block (█)</option>
                    <option value="underline">Underline (_)</option>
                  </select>
                </div>

                <div>
                  <label className="text-xs font-semibold text-text-muted block mb-1">
                    {t('settings.lineHeight', 'Line Height')}
                  </label>
                  <input
                    type="number"
                    step="0.1"
                    min={1.2}
                    max={2.2}
                    value={settings.lineHeight}
                    onChange={(e) => updateSetting('lineHeight', Number(e.target.value))}
                    className="w-full bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                  />
                </div>
              </div>

              {/* Live Preview Box */}
              <div className="p-4 rounded-xl border border-border bg-bg/70">
                <div className="text-xs font-mono text-text-muted mb-2 uppercase">
                  {t('settings.livePreview', 'Live Preview')}
                </div>
                <pre
                  className="font-mono text-accent"
                  style={{
                    fontFamily: settings.fontFamily,
                    fontSize: `${settings.fontSize}px`,
                    lineHeight: settings.lineHeight,
                  }}
                >
                  {`function calculateWpm(chars: number, seconds: number): number {\n  return Math.round((chars / 5) / (seconds / 60));\n}`}
                </pre>
              </div>
            </div>
          )}

          {/* Keyboard Section */}
          {activeSection === 'keyboard' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.keyboard', 'Keyboard & Layout')}
              </h3>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">{t('settings.layout', 'Layout')}</div>
                  <div className="text-xs text-text-muted">
                    {t('settings.layoutDesc', 'Physical keyboard arrangement')}
                  </div>
                </div>
                <select
                  value={settings.layout}
                  onChange={(e) => updateSetting('layout', e.target.value)}
                  className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                >
                  <option value="ansi-us">ANSI US</option>
                  <option value="iso-uk">ISO UK</option>
                  <option value="dvorak">Dvorak</option>
                  <option value="colemak">Colemak</option>
                </select>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.showVirtualKeyboard', 'Virtual Keyboard')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.showVirtualKeyboardDesc', 'On-screen visual keyboard display')}
                  </div>
                </div>
                <select
                  value={settings.showVirtualKeyboard}
                  onChange={(e) => updateSetting('showVirtualKeyboard', e.target.value)}
                  className="bg-surface-2 border border-border text-sm rounded-lg px-3 py-1.5 text-text"
                >
                  <option value="auto">Auto (Hide on mobile)</option>
                  <option value="always">Always Show</option>
                  <option value="never">Never Show</option>
                </select>
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.fingerGuide', 'Finger Color Guidance')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.fingerGuideDesc', 'Colors key columns corresponding to touch-typing finger zones')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.fingerGuide}
                  onChange={(e) => updateSetting('fingerGuide', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>
            </div>
          )}

          {/* Account Section (AC-UM-8) */}
          {activeSection === 'account' && (
            <form onSubmit={handleUpdateProfile} className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.account', 'Account Details')}
              </h3>

              {accountMsg && (
                <div className="p-3 rounded-lg border border-accent/40 bg-accent/10 text-xs text-accent">
                  {accountMsg}
                </div>
              )}

              <div>
                <label className="text-xs font-semibold text-text-muted block mb-1">
                  {t('auth.username', 'Username')}
                </label>
                <input
                  type="text"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  className="w-full bg-surface-2 border border-border text-sm rounded-lg px-3 py-2 text-text"
                />
                <p className="text-[11px] text-text-muted mt-1">
                  {t(
                    'settings.usernameThrottleNotice',
                    'Note: Username can only be updated once every 30 days.'
                  )}
                </p>
              </div>

              <div>
                <label className="text-xs font-semibold text-text-muted block mb-1">
                  {t('auth.email', 'Email')}
                </label>
                <input
                  type="email"
                  value={email}
                  disabled
                  className="w-full bg-surface-3 border border-border text-sm rounded-lg px-3 py-2 text-text-muted cursor-not-allowed"
                />
              </div>

              <button
                type="submit"
                className="px-4 py-2 bg-accent text-accent-fg font-semibold rounded-lg text-sm hover:opacity-90"
              >
                {t('actions.save', 'Save Changes')}
              </button>
            </form>
          )}

          {/* Privacy & Export Section (AC-UM-11) */}
          {activeSection === 'privacy' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-text pb-2 border-b border-border">
                {t('settings.privacy', 'Privacy & Data Ownership')}
              </h3>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.profilePublic', 'Public Profile')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.profilePublicDesc', 'Allow other developers to view your profile and rank')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.profilePublic}
                  onChange={(e) => updateSetting('profilePublic', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>

              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-semibold text-text">
                    {t('settings.showOnLeaderboard', 'Show on Leaderboards')}
                  </div>
                  <div className="text-xs text-text-muted">
                    {t('settings.showOnLeaderboardDesc', 'Display your best Speed Test scores on public boards')}
                  </div>
                </div>
                <input
                  type="checkbox"
                  checked={settings.showOnLeaderboard}
                  onChange={(e) => updateSetting('showOnLeaderboard', e.target.checked)}
                  className="h-4 w-4 accent-accent rounded"
                />
              </div>

              <div className="pt-4 border-t border-border">
                <div className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-semibold text-text">
                      {t('settings.exportData', 'Export Personal Data (GDPR)')}
                    </div>
                    <div className="text-xs text-text-muted">
                      {t(
                        'settings.exportDataDesc',
                        'Download full JSON export of your profile, training sessions, and gamification ledger.'
                      )}
                    </div>
                  </div>
                  <button
                    onClick={handleExportData}
                    className="px-4 py-2 bg-surface-2 hover:bg-surface-3 border border-border rounded-lg text-sm font-semibold text-text"
                  >
                    📥 {t('settings.downloadJson', 'Download JSON')}
                  </button>
                </div>
              </div>
            </div>
          )}

          {/* Danger Zone (AC-UM-11) */}
          {activeSection === 'danger' && (
            <div className="space-y-5">
              <h3 className="text-base font-bold text-red-400 pb-2 border-b border-red-500/30">
                {t('settings.dangerZone', 'Danger Zone')}
              </h3>

              {deleteStatus && (
                <div className="p-4 rounded-xl border border-amber-500/40 bg-amber-500/10 text-sm text-amber-300">
                  {deleteStatus}
                </div>
              )}

              <div className="p-4 rounded-xl border border-red-500/30 bg-red-500/10 flex items-center justify-between">
                <div>
                  <div className="text-sm font-bold text-red-400">
                    {t('settings.deleteAccountTitle', 'Delete Account')}
                  </div>
                  <div className="text-xs text-red-300/80 mt-1 max-w-md">
                    {t(
                      'settings.deleteAccountDesc',
                      'Initiates a 30-day grace period, revokes active session tokens, and marks your data for permanent anonymization.'
                    )}
                  </div>
                </div>
                <button
                  onClick={() => setShowDeleteModal(true)}
                  className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white text-sm font-bold rounded-lg transition-colors"
                >
                  {t('settings.deleteAccountBtn', 'Delete Account')}
                </button>
              </div>

              {/* Confirmation Dialog */}
              {showDeleteModal && (
                <div className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                  <div className="bg-surface border border-red-500/40 rounded-2xl max-w-md w-full p-6 space-y-4">
                    <h3 className="text-lg font-bold text-red-400">
                      {t('settings.confirmDeleteTitle', 'Confirm Account Deletion')}
                    </h3>
                    <p className="text-sm text-text-muted">
                      {t(
                        'settings.confirmDeleteDesc',
                        'Are you sure you want to schedule your account for permanent deletion? You will be immediately logged out.'
                      )}
                    </p>
                    <div className="flex justify-end gap-3 pt-3">
                      <button
                        onClick={() => setShowDeleteModal(false)}
                        className="px-4 py-2 rounded-lg bg-surface-2 border border-border text-sm text-text font-medium"
                      >
                        {t('actions.cancel', 'Cancel')}
                      </button>
                      <button
                        onClick={() => {
                          setShowDeleteModal(false);
                          handleDeleteAccount();
                        }}
                        className="px-4 py-2 rounded-lg bg-red-600 hover:bg-red-700 text-white text-sm font-bold"
                      >
                        {t('settings.yesDelete', 'Yes, Delete My Account')}
                      </button>
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
