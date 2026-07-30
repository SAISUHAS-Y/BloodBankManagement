import React, { useState } from 'react';
import { Outlet, NavLink, useLocation } from 'react-router';
import { useAuth } from '../features/auth/context/AuthContext';
import { AdminNavbar } from './AdminNavbar';
import {
  Building2,
  Boxes,
  HeartHandshake,
  FileSpreadsheet,
  Users,
  ShieldCheck,
  ChevronRight,
  X,
} from 'lucide-react';
import { cn } from '../shared/components/ui/utils';

interface NavItem {
  title: string;
  href: string;
  icon: React.ElementType;
  requiredRole?: string[];
  requiredPermission?: string;
}

const NAV_ITEMS: NavItem[] = [
  {
    title: 'Blood Inventory',
    href: '/inventory',
    icon: Boxes,
  },
  {
    title: 'Hospitals',
    href: '/hospitals',
    icon: Building2,
  },
  {
    title: 'Donations',
    href: '/donations',
    icon: HeartHandshake,
  },
  {
    title: 'Blood Requests',
    href: '/requests',
    icon: FileSpreadsheet,
  },
  {
    title: 'Users & Donors',
    href: '/users',
    icon: Users,
  },
  {
    title: 'Admin RBAC',
    href: '/admin/roles',
    icon: ShieldCheck,
  },
];

export const AdminLayout: React.FC = () => {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, hasPermission } = useAuth();
  const location = useLocation();

  const filterNavItems = (items: NavItem[]) => {
    return items.filter((item) => {
      if (item.requiredPermission && !hasPermission(item.requiredPermission)) {
        return false;
      }
      return true;
    });
  };

  const visibleNavItems = filterNavItems(NAV_ITEMS);

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 flex flex-col font-sans">
      <AdminNavbar onToggleSidebar={() => setSidebarOpen(!sidebarOpen)} />

      <div className="flex flex-1 overflow-hidden">
        {/* Mobile Sidebar Overlay */}
        {sidebarOpen && (
          <div
            className="fixed inset-0 z-40 bg-slate-900/50 backdrop-blur-xs lg:hidden"
            onClick={() => setSidebarOpen(false)}
          />
        )}

        {/* Sidebar Navigation */}
        <aside
          className={cn(
            'fixed inset-y-0 left-0 z-40 w-64 transform bg-white border-r border-slate-200 p-4 transition-transform duration-200 ease-in-out dark:bg-slate-900 dark:border-slate-800 lg:static lg:translate-x-0 flex flex-col justify-between pt-20 lg:pt-4',
            sidebarOpen ? 'translate-x-0' : '-translate-x-full'
          )}
        >
          <div className="space-y-6">
            <div className="flex items-center justify-between lg:hidden px-2 pb-2 border-b border-slate-100 dark:border-slate-800">
              <span className="text-sm font-bold text-slate-800 dark:text-slate-200">
                Navigation Menu
              </span>
              <button
                onClick={() => setSidebarOpen(false)}
                className="text-slate-500 hover:text-slate-800 dark:hover:text-white"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <nav className="space-y-1">
              {visibleNavItems.map((item) => {
                const Icon = item.icon;
                const isActive = location.pathname.startsWith(item.href);
                return (
                  <NavLink
                    key={item.href}
                    to={item.href}
                    onClick={() => setSidebarOpen(false)}
                    className={cn(
                      'group flex items-center justify-between rounded-lg px-3 py-2.5 text-sm font-medium transition-colors',
                      isActive
                        ? 'bg-rose-50 text-rose-700 dark:bg-rose-950/60 dark:text-rose-300 font-semibold'
                        : 'text-slate-600 hover:bg-slate-100 hover:text-slate-900 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-slate-100'
                    )}
                  >
                    <div className="flex items-center gap-3">
                      <Icon
                        className={cn(
                          'h-4 w-4 transition-colors',
                          isActive
                            ? 'text-rose-600 dark:text-rose-400'
                            : 'text-slate-400 group-hover:text-slate-600 dark:group-hover:text-slate-300'
                        )}
                      />
                      <span>{item.title}</span>
                    </div>
                    {isActive && <ChevronRight className="h-4 w-4 text-rose-500" />}
                  </NavLink>
                );
              })}
            </nav>
          </div>

          {/* Sidebar Footer Info */}
          <div className="rounded-xl bg-slate-50 dark:bg-slate-950 p-3.5 border border-slate-200/60 dark:border-slate-800/60">
            <p className="text-xs font-medium text-slate-500 dark:text-slate-400">
              Logged in as:
            </p>
            <p className="text-xs font-bold text-slate-800 dark:text-slate-200 truncate mt-0.5">
              {user?.fullName || user?.username}
            </p>
          </div>
        </aside>

        {/* Main Content Viewport */}
        <main className="flex-1 overflow-y-auto p-4 sm:p-6 lg:p-8">
          <div className="mx-auto max-w-7xl">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};
