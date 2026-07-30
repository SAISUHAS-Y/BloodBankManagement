import React from 'react';

export interface DemoAccountCardProps {
  roleLabel: string;
  username: string;
  colorScheme: 'red' | 'blue' | 'green' | 'amber';
  onSelect: (username: string) => void;
}

const colorMap = {
  red: {
    badge: 'text-rose-400 font-bold',
    hoverBorder: 'hover:border-rose-500/50 hover:bg-rose-950/30',
  },
  blue: {
    badge: 'text-sky-400 font-bold',
    hoverBorder: 'hover:border-sky-500/50 hover:bg-sky-950/30',
  },
  green: {
    badge: 'text-emerald-400 font-bold',
    hoverBorder: 'hover:border-emerald-500/50 hover:bg-emerald-950/30',
  },
  amber: {
    badge: 'text-amber-400 font-bold',
    hoverBorder: 'hover:border-amber-500/50 hover:bg-amber-950/30',
  },
};

export const DemoAccountCard: React.FC<DemoAccountCardProps> = ({
  roleLabel,
  username,
  colorScheme,
  onSelect,
}) => {
  const styles = colorMap[colorScheme];

  return (
    <button
      type="button"
      onClick={() => onSelect(username)}
      className={`group flex flex-col justify-center rounded-xl bg-slate-950/60 border border-slate-800 p-3 text-left transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-rose-500 ${styles.hoverBorder}`}
      aria-label={`Select ${roleLabel} demo credentials for ${username}`}
    >
      <span className={`text-xs ${styles.badge} group-hover:scale-105 transition-transform`}>
        {roleLabel}
      </span>
      <span className="font-mono text-xs text-slate-400 group-hover:text-slate-200 transition-colors mt-0.5">
        {username}
      </span>
    </button>
  );
};

export default DemoAccountCard;
