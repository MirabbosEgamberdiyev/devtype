import React from 'react';

interface SparklineProps {
  data: Array<{ date: string; wpm: number }>;
  width?: number;
  height?: number;
  className?: string;
  color?: string;
}

export const Sparkline: React.FC<SparklineProps> = ({
  data,
  width = 120,
  height = 36,
  className = '',
  color = 'var(--accent, #3b82f6)',
}) => {
  if (!data || data.length === 0) {
    return (
      <div
        className={`flex items-center justify-center text-xs text-text-muted ${className}`}
        style={{ width, height }}
      >
        —
      </div>
    );
  }

  const values = data.map((d) => d.wpm);
  const min = Math.min(...values, 0);
  const max = Math.max(...values, 50);
  const range = max - min || 1;

  const padding = 4;
  const innerWidth = width - padding * 2;
  const innerHeight = height - padding * 2;

  const points = data
    .map((d, index) => {
      const x = padding + (index / Math.max(data.length - 1, 1)) * innerWidth;
      const y = height - padding - ((d.wpm - min) / range) * innerHeight;
      return `${x.toFixed(1)},${y.toFixed(1)}`;
    })
    .join(' ');

  const firstPoint = `${padding},${height - padding}`;
  const lastPoint = `${width - padding},${height - padding}`;
  const fillPoints = `${firstPoint} ${points} ${lastPoint}`;

  return (
    <svg
      width={width}
      height={height}
      className={`overflow-visible ${className}`}
      aria-label="14-day WPM sparkline"
    >
      <polygon points={fillPoints} fill={color} fillOpacity={0.15} />
      <polyline
        points={points}
        fill="none"
        stroke={color}
        strokeWidth={2}
        strokeLinecap="round"
        strokeLinejoin="round"
      />
      {data.length > 0 && (
        <circle
          cx={points.split(' ').pop()?.split(',')[0]}
          cy={points.split(' ').pop()?.split(',')[1]}
          r={3}
          fill={color}
        />
      )}
    </svg>
  );
};
