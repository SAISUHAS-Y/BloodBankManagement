import React from 'react';
import { Outlet } from 'react-router';

export const RootLayout: React.FC = () => {
  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans antialiased">
      <main className="flex-1 flex flex-col">
        <Outlet />
      </main>
    </div>
  );
};
