import React from 'react';

export interface PasswordStrengthMeterProps {
  password?: string;
}

export const PasswordStrengthMeter: React.FC<PasswordStrengthMeterProps> = ({ password = '' }) => {
  const getStrength = (pwd: string) => {
    if (!pwd) return { score: 0, label: '', color: 'bg-slate-800' };
    let score = 0;
    if (pwd.length >= 8) score++;
    if (/[A-Z]/.test(pwd)) score++;
    if (/[0-9]/.test(pwd)) score++;
    if (/[^A-Za-z0-9]/.test(pwd)) score++;

    if (score <= 1) return { score: 1, label: 'Weak', color: 'bg-red-500', textColor: 'text-red-400' };
    if (score <= 3) return { score: 2, label: 'Medium', color: 'bg-amber-500', textColor: 'text-amber-400' };
    return { score: 3, label: 'Strong', color: 'bg-emerald-500', textColor: 'text-emerald-400' };
  };

  const strength = getStrength(password);

  if (!password) return null;

  return (
    <div className="space-y-1 pt-1">
      <div className="flex items-center justify-between text-[11px]">
        <span className="text-slate-400">Password Strength:</span>
        <span className={`font-semibold ${strength.textColor}`}>{strength.label}</span>
      </div>
      <div className="grid grid-cols-3 gap-1.5 h-1.5 w-full">
        <div className={`rounded-full transition-colors ${strength.score >= 1 ? strength.color : 'bg-slate-800'}`} />
        <div className={`rounded-full transition-colors ${strength.score >= 2 ? strength.color : 'bg-slate-800'}`} />
        <div className={`rounded-full transition-colors ${strength.score >= 3 ? strength.color : 'bg-slate-800'}`} />
      </div>
    </div>
  );
};

export default PasswordStrengthMeter;
