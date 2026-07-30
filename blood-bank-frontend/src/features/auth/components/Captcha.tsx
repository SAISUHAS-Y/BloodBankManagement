import React from 'react';
import { CheckSquare, Square, ShieldCheck } from 'lucide-react';

export interface CaptchaProps {
  verified: boolean;
  onChange: (verified: boolean) => void;
}

export const Captcha: React.FC<CaptchaProps> = ({ verified, onChange }) => {
  return (
    <div className="flex items-center justify-between bg-slate-950/60 border border-slate-800 rounded-lg p-3 text-xs">
      <label className="flex items-center gap-2.5 text-slate-300 cursor-pointer select-none">
        <button
          type="button"
          role="checkbox"
          aria-checked={verified}
          onClick={() => onChange(!verified)}
          className="text-rose-500 focus:outline-none"
        >
          {verified ? (
            <CheckSquare className="h-5 w-5 fill-rose-600 text-slate-950" />
          ) : (
            <Square className="h-5 w-5 text-slate-600 hover:text-slate-400" />
          )}
        </button>
        <span className="font-medium">I'm not a robot</span>
      </label>
      <div className="flex items-center gap-1 text-[10px] text-slate-500 font-mono">
        <ShieldCheck className="h-3.5 w-3.5 text-rose-500" />
        <span>reCAPTCHA</span>
      </div>
    </div>
  );
};

export default Captcha;
