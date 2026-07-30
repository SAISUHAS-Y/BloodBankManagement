import React, { useState } from 'react';
import { useAuth } from '../features/auth/context/AuthContext';
import { Badge } from '../shared/components/ui/badge';
import { Button } from '../shared/components/ui/button';
import { NotificationBell } from '../features/notifications/components/NotificationBell';
import {
  HeartPulse,
  LogOut,
  User as UserIcon,
  Bell,
  Menu,
  ShieldCheck,
} from 'lucide-react';

interface AdminNavbarProps {
  onToggleSidebar?: () => void;
}

export const AdminNavbar: React.FC<AdminNavbarProps> = ({ onToggleSidebar }) => {
  const { user, logout } = useAuth();
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  const getRoleBadgeVariant = (roles: string[] = []) => {
    const roleStr = roles.join(' ').toLowerCase();
    if (roleStr.includes('admin')) return 'destructive';
    if (roleStr.includes('hospital')) return 'secondary';
    if (roleStr.includes('bank')) return 'default';
    return 'outline';
  };

  const primaryRole = user?.roles?.[0] || 'User';

  return (
    <header className="sticky top-0 z-30 flex h-16 w-full items-center justify-between border-b border-slate-200 bg-white/95 px-4 shadow-xs backdrop-blur-md dark:border-slate-800 dark:bg-slate-900/95 sm:px-6">
      <div className="flex items-center gap-3">
        <Button
          variant="ghost"
          size="icon"
          className="lg:hidden"
          onClick={onToggleSidebar}
        >
          <Menu className="h-5 w-5 text-slate-600 dark:text-slate-300" />
          <span className="sr-only">Toggle Sidebar</span>
        </Button>

        <div className="flex items-center gap-2">
          <div className="flex h-9 w-9 items-center justify-center rounded-lg bg-rose-600 text-white shadow-sm">
            <HeartPulse className="h-5 w-5" />
          </div>
          <span className="text-lg font-bold tracking-tight text-slate-900 dark:text-white hidden sm:inline-block">
            Blood Bank Portal
          </span>
        </div>
      </div>

      <div className="flex items-center gap-3">
        {/* Notification Bell Component */}
        <NotificationBell />

        {/* User Profile Dropdown */}
        <div className="relative">
          <button
            onClick={() => setUserMenuOpen(!userMenuOpen)}
            className="flex items-center gap-2.5 rounded-full p-1 transition-colors hover:bg-slate-100 dark:hover:bg-slate-800 focus:outline-none"
          >
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-rose-100 text-rose-700 font-semibold text-xs dark:bg-rose-950 dark:text-rose-300 border border-rose-200 dark:border-rose-800">
              {user?.firstName?.[0] || 'U'}
              {user?.lastName?.[0] || ''}
            </div>
            <div className="hidden text-left sm:block">
              <p className="text-xs font-semibold text-slate-800 dark:text-slate-200 leading-tight">
                {user?.firstName} {user?.lastName}
              </p>
              <p className="text-[10px] text-slate-500 dark:text-slate-400">
                @{user?.username}
              </p>
            </div>
          </button>

          {userMenuOpen && (
            <div
              className="absolute right-0 mt-2 w-56 rounded-xl border border-slate-200 bg-white p-2 shadow-xl dark:border-slate-800 dark:bg-slate-900 z-50 animate-in fade-in-80 zoom-in-95"
              onClick={() => setUserMenuOpen(false)}
            >
              <div className="px-3 py-2 border-b border-slate-100 dark:border-slate-800">
                <p className="text-sm font-semibold text-slate-900 dark:text-slate-100">
                  {user?.firstName} {user?.lastName}
                </p>
                <p className="text-xs text-slate-500 dark:text-slate-400 truncate">
                  {user?.email}
                </p>
                <div className="mt-2 flex items-center gap-1.5">
                  <Badge variant={getRoleBadgeVariant(user?.roles)} className="text-[10px] uppercase tracking-wider">
                    <ShieldCheck className="h-3 w-3 mr-1" />
                    {primaryRole}
                  </Badge>
                </div>
              </div>

              <div className="pt-1">
                <button
                  onClick={logout}
                  className="flex w-full items-center gap-2 rounded-lg px-3 py-2 text-sm font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/50 transition-colors"
                >
                  <LogOut className="h-4 w-4" />
                  Logout
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};
