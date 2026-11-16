import React from 'react';
import { useTranslation } from 'react-i18next';
import { ActivityDay } from '../api/devtypeApi';

interface ActivityCalendarProps {
  days: ActivityDay[];
  className?: string;
}

export const ActivityCalendar: React.FC<ActivityCalendarProps> = ({ days, className = '' }) => {
  const { t } = useTranslation('common');

  // Map dates to lookup dictionary
  const dayMap = React.useMemo(() => {
    const map = new Map<string, ActivityDay>();
    for (const d of days) {
      map.set(d.date, d);
    }
    return map;
  }, [days]);

  // Generate 52 weeks (364-365 days) leading up to today
  const weeks = React.useMemo(() => {
    const result: Array<Array<{ date: string; dayData?: ActivityDay }>> = [];
    const today = new Date();
    // End on current week Saturday or today
    const endDate = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const dayOfWeek = endDate.getDay(); // 0 is Sunday
    const totalDays = 52 * 7 + (dayOfWeek + 1);

    const startDate = new Date(endDate);
    startDate.setDate(endDate.getDate() - totalDays + 1);

    let currentWeek: Array<{ date: string; dayData?: ActivityDay }> = [];
    const cur = new Date(startDate);

    while (cur <= endDate) {
      const dateStr = cur.toISOString().slice(0, 10);
      currentWeek.push({
        date: dateStr,
        dayData: dayMap.get(dateStr),
      });

      if (cur.getDay() === 6 || cur.getTime() === endDate.getTime()) {
        result.push(currentWeek);
        currentWeek = [];
      }
      cur.setDate(cur.getDate() + 1);
    }

    if (currentWeek.length > 0) {
      result.push(currentWeek);
    }
    return result;
  }, [dayMap]);

  const getCellColor = (level?: number) => {
    switch (level) {
      case 1:
        return 'bg-emerald-900/40 border-emerald-700/60 text-emerald-300';
      case 2:
        return 'bg-emerald-700/60 border-emerald-500 text-emerald-200';
      case 3:
        return 'bg-emerald-500 border-emerald-400 text-white';
      case 4:
        return 'bg-emerald-400 border-emerald-300 text-emerald-950';
      default:
        return 'bg-surface-2 border-border/40';
    }
  };

  return (
    <div className={`p-4 rounded-xl border border-border bg-surface ${className}`}>
      <div className="flex items-center justify-between mb-3">
        <h3 className="text-sm font-semibold text-text flex items-center gap-2">
          <span>📅</span>
          <span>{t('analytics.activityCalendar', 'Activity Calendar (365 Days)')}</span>
        </h3>
        <div className="flex items-center gap-1.5 text-xs text-text-muted">
          <span>{t('analytics.less', 'Less')}</span>
          <span className="w-2.5 h-2.5 rounded-sm bg-surface-2 border border-border/40" />
          <span className="w-2.5 h-2.5 rounded-sm bg-emerald-900/40 border border-emerald-700/60" />
          <span className="w-2.5 h-2.5 rounded-sm bg-emerald-700/60 border border-emerald-500" />
          <span className="w-2.5 h-2.5 rounded-sm bg-emerald-500 border border-emerald-400" />
          <span className="w-2.5 h-2.5 rounded-sm bg-emerald-400 border border-emerald-300" />
          <span>{t('analytics.more', 'More')}</span>
        </div>
      </div>

      <div className="overflow-x-auto pb-2">
        <div className="flex gap-1 min-w-[720px]">
          {weeks.map((week, wIdx) => (
            <div key={wIdx} className="flex flex-col gap-1">
              {week.map((day) => {
                const level = day.dayData ? day.dayData.level : 0;
                const minutes = day.dayData ? day.dayData.minutesTrained : 0;
                const sessions = day.dayData ? day.dayData.sessionsCount : 0;
                const title = `${day.date}: ${sessions} sessions (${minutes} min)`;

                return (
                  <div
                    key={day.date}
                    title={title}
                    aria-label={title}
                    className={`w-3 h-3 rounded-sm border cursor-pointer transition-colors ${getCellColor(level)}`}
                  />
                );
              })}
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
