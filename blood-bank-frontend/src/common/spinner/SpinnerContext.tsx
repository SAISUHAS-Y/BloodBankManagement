import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { configureSpinner } from '../../api/http';
import { Loader2 } from 'lucide-react';

interface SpinnerContextType {
  isLoading: boolean;
  showSpinner: () => void;
  hideSpinner: () => void;
}

const SpinnerContext = createContext<SpinnerContextType | undefined>(undefined);

export const SpinnerProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [activeRequests, setActiveRequests] = useState(0);

  const showSpinner = useCallback(() => {
    setActiveRequests((prev) => prev + 1);
  }, []);

  const hideSpinner = useCallback(() => {
    setActiveRequests((prev) => Math.max(0, prev - 1));
  }, []);

  useEffect(() => {
    configureSpinner(showSpinner, hideSpinner);
  }, [showSpinner, hideSpinner]);

  const isLoading = activeRequests > 0;

  return (
    <SpinnerContext.Provider value={{ isLoading, showSpinner, hideSpinner }}>
      {children}
      {isLoading && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/30 backdrop-blur-xs transition-opacity duration-200">
          <div className="flex flex-col items-center gap-3 rounded-xl bg-white dark:bg-slate-900 p-6 shadow-2xl border border-slate-200 dark:border-slate-800">
            <div className="relative flex items-center justify-center">
              <Loader2 className="h-10 w-10 animate-spin text-rose-600 dark:text-rose-500" />
              <span className="absolute h-4 w-4 rounded-full bg-rose-500/20 animate-ping" />
            </div>
            <p className="text-sm font-medium text-slate-700 dark:text-slate-300 animate-pulse">
              Processing request...
            </p>
          </div>
        </div>
      )}
    </SpinnerContext.Provider>
  );
};

export const useSpinner = (): SpinnerContextType => {
  const context = useContext(SpinnerContext);
  if (!context) {
    throw new Error('useSpinner must be used within a SpinnerProvider');
  }
  return context;
};
