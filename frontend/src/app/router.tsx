import { createBrowserRouter } from 'react-router-dom';
import { PublicLayout } from './layouts/PublicLayout';
import { AuthLayout } from './layouts/AuthLayout';
import { GuestOnly } from './guards/GuestOnly';
import { RequireAuth } from './guards/RequireAuth';
import { LandingPage } from '../features/landing/LandingPage';
import { TypingView } from '../features/typing/TypingView';
import { LanguagesPage } from '../features/languages';
import { OnboardingPage } from '../features/onboarding';
import { SnippetEditorPage } from '../features/admin';
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
        element: <TypingView />,
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
        path: 'dashboard',
        element: <TypingView />,
      },
      {
        path: 'onboarding',
        element: <OnboardingPage />,
      },
      {
        path: 'admin/exercises',
        element: <SnippetEditorPage />,
      },
    ],
  },
]);
