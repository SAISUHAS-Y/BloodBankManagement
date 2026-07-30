import React from 'react';
import { Card, CardHeader, CardTitle, CardDescription } from '../../../shared/components/ui/card';
import { HeartPulse } from 'lucide-react';
import loginBgSvg from '../../../assets/images/login-bg.svg';

export interface AuthLayoutProps {
  title: string;
  subtitle: string;
  children: React.ReactNode;
  maxWidthClass?: string;
}

export const AuthLayout: React.FC<AuthLayoutProps> = ({
  title,
  subtitle,
  children,
  maxWidthClass = 'max-w-[480px]',
}) => {
  return (
    <div className="relative min-h-screen w-full flex flex-col items-center justify-center p-4 sm:p-6 overflow-hidden bg-[#0a0a12] text-slate-100 selection:bg-rose-500 selection:text-white">
      {/* Background Layer */}
      <div className="absolute inset-0 z-0 overflow-hidden pointer-events-none">
        <picture>
          <img
            src={loginBgSvg}
            alt="Blood Bank Theme Backdrop"
            loading="eager"
            // @ts-ignore
            fetchpriority="high"
            className="h-full w-full object-cover object-center scale-105 opacity-25"
          />
        </picture>
        <div className="absolute inset-0 bg-gradient-to-br from-[#0a0a12]/95 via-[#0a0a12]/90 to-red-950/60" />
        <div className="absolute -top-40 -left-40 h-[500px] w-[500px] rounded-full bg-rose-900/30 blur-[120px] animate-pulse" />
        <div className="absolute -bottom-40 -right-40 h-[500px] w-[500px] rounded-full bg-rose-950/40 blur-[120px] animate-pulse" />
      </div>

      {/* Main Glassmorphism Card */}
      <Card
        className={`relative z-10 w-full ${maxWidthClass} bg-[#12131c]/90 border-slate-800/80 backdrop-blur-2xl text-slate-100 shadow-2xl shadow-rose-950/40 rounded-2xl p-6 sm:p-10 transition-all duration-300 my-8`}
      >
        {/* Header */}
        <CardHeader className="space-y-4 text-center pb-4 p-0">
          <div className="mx-auto flex h-20 w-20 items-center justify-center rounded-2xl bg-gradient-to-br from-rose-600 to-rose-900 border border-rose-500/30 text-white shadow-lg shadow-rose-950/60 group">
            <HeartPulse className="h-10 w-10 text-white animate-pulse group-hover:scale-110 transition-transform" />
          </div>
          <div>
            <CardTitle className="text-2xl sm:text-3xl font-bold tracking-tight text-white">
              {title}
            </CardTitle>
            <CardDescription className="text-slate-400 text-xs sm:text-sm mt-1">
              {subtitle}
            </CardDescription>
          </div>
        </CardHeader>

        {children}
      </Card>

      {/* Environment Badge */}
      <div className="relative z-10 text-center pb-4">
        <span className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-slate-900/80 border border-slate-800 text-[11px] font-mono text-slate-500">
          <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
          v1.0.0 · staging
        </span>
      </div>
    </div>
  );
};

export default AuthLayout;
