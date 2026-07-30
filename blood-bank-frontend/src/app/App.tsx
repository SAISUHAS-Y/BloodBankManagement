import React from 'react';
import { RouterProvider } from 'react-router';
import { router } from './routes';
import { ToastProvider } from '../common/toast/ToastProvider';
import { ModalProvider } from '../common/modal/ModalContext';
import { SpinnerProvider } from '../common/spinner/SpinnerContext';
import { AuthProvider } from '../features/auth/context/AuthContext';

export default function App() {
  return (
    <ToastProvider>
      <ModalProvider>
        <SpinnerProvider>
          <AuthProvider>
            <RouterProvider router={router} />
          </AuthProvider>
        </SpinnerProvider>
      </ModalProvider>
    </ToastProvider>
  );
}
