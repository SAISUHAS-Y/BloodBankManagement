import React from 'react';
import { Toaster, toast as sonnerToast } from 'sonner';

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  return (
    <>
      {children}
      <Toaster
        position="top-right"
        richColors
        closeButton
        toastOptions={{
          className: 'font-sans rounded-lg shadow-lg border',
          duration: 4000,
        }}
      />
    </>
  );
};

export const toast = {
  success: (message: string, description?: string) => sonnerToast.success(message, { description }),
  error: (message: string, description?: string) => sonnerToast.error(message, { description }),
  info: (message: string, description?: string) => sonnerToast.info(message, { description }),
  warning: (message: string, description?: string) => sonnerToast.warning(message, { description }),
};

export const useToast = () => toast;
