import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { TypingView } from '../typing/TypingView';

export const PracticePage: React.FC = () => {
  const { lang, topic } = useParams<{ lang?: string; topic?: string }>();
  const navigate = useNavigate();

  return (
    <div className="w-full">
      <TypingView
        language={lang || 'java'}
        topic={topic}
        mode="PRACTICE"
        onBackToTopics={() => navigate('/languages')}
      />
    </div>
  );
};
