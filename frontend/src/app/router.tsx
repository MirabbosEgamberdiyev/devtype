import { createBrowserRouter } from 'react-router-dom';
import { PublicLayout } from './layouts/PublicLayout';
import { AuthLayout } from './layouts/AuthLayout';
import { AppLayout } from './layouts/AppLayout';
import { GuestOnly } from './guards/GuestOnly';
import { RequireAuth } from './guards/RequireAuth';
import { LandingPage } from '../features/landing/LandingPage';
import { TypingView } from '../features/typing/TypingView';
import { PracticePage, SpeedTestPage, SpecialCharsPage } from '../features/practice';
import { LanguagesPage } from '../features/languages';
import { OnboardingPage } from '../features/onboarding';
import { SnippetEditorPage } from '../features/admin';
import { DashboardPage } from '../features/dashboard/DashboardPage';
import { HistoryPage } from '../features/history/HistoryPage';
import { AnalyticsPage } from '../features/analytics/AnalyticsPage';
import { SettingsPage } from '../features/settings/SettingsPage';
import { ProfilePage } from '../features/profile/ProfilePage';
import {
  LoginPage,
  RegisterPage,
  ForgotPasswordPage,
  ResetPasswordPage,
  VerifyEmailPage,
  OAuthCallbackPage,
} from '../features/auth';

export const router = createBrowserRouter([
  {
    path: '/',
    element: <PublicLayout />,
    children: [
      {
        index: true,
        element: <LandingPage />,
      },
      {
        path: 'languages',
        element: <LanguagesPage />,
      },
      {
        path: 'practice',
        element: <PracticePage />,
      },
      {
        path: 'practice/:lang',
        element: <PracticePage />,
      },
      {
        path: 'practice/:lang/:topic',
        element: <PracticePage />,
      },
      {
        path: 'practice/:lang/special',
        element: <SpecialCharsPage />,
      },
      {
        path: 'speed-test',
        element: <SpeedTestPage />,
      },
      {
        path: 'speed-test/:lang',
        element: <SpeedTestPage />,
      },
      {
        path: 'typing',
        element: <TypingView />,
      },
    ],
  },
  {
    element: <AuthLayout />,
    children: [
      {
        element: <GuestOnly />,
        children: [
          {
            path: 'login',
            element: <LoginPage />,
          },
          {
            path: 'register',
            element: <RegisterPage />,
          },
          {
            path: 'forgot-password',
            element: <ForgotPasswordPage />,
          },
          {
            path: 'reset-password',
            element: <ResetPasswordPage />,
          },
        ],
      },
      {
        path: 'verify-email',
        element: <VerifyEmailPage />,
      },
      {
        path: 'oauth/callback',
        element: <OAuthCallbackPage />,
      },
    ],
  },
  {
    element: <RequireAuth />,
    children: [
      {
        path: 'onboarding',
        element: <OnboardingPage />,
      },
      {
        element: <AppLayout />,
        children: [
          {
            path: 'dashboard',
            element: <DashboardPage />,
          },
          {
            path: 'history',
            element: <HistoryPage />,
          },
          {
            path: 'analytics',
            element: <AnalyticsPage />,
          },
          {
            path: 'analytics/:lang',
            element: <AnalyticsPage />,
          },
          {
            path: 'settings',
            element: <SettingsPage />,
          },
          {
            path: 'settings/:section',
            element: <SettingsPage />,
          },
          {
            path: 'profile',
            element: <ProfilePage />,
          },
          {
            path: 'admin/exercises',
            element: <SnippetEditorPage />,
          },
        ],
      },
    ],
  },
]);
