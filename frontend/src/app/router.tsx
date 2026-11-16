import { createBrowserRouter } from 'react-router-dom';
import { PublicLayout } from './layouts/PublicLayout';
import { LandingPage } from '../features/landing/LandingPage';
import { TypingView } from '../features/typing/TypingView';

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
        path: 'practice',
        element: <TypingView />,
      },
      {
        path: 'typing',
        element: <TypingView />,
      },
    ],
  },
]);
