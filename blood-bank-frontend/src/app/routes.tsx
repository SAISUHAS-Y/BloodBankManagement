import React, { lazy, Suspense } from 'react';
import { createBrowserRouter, Navigate, Outlet } from 'react-router';
import { useAuth } from '../features/auth/context/AuthContext';
import { RootLayout } from '../layouts/RootLayout';
import { AdminLayout } from '../layouts/AdminLayout';
import { NotFoundPage } from './NotFoundPage';
import { Loader2 } from 'lucide-react';

// Lazy-loaded feature pages with explicit default export resolution
const LoginPage = lazy(() => import('../features/auth/pages/LoginPage').then(m => ({ default: m.default || m.LoginPage })));
const RegisterPage = lazy(() => import('../features/auth/pages/RegisterPage').then(m => ({ default: m.default || m.RegisterPage })));
const VerifyEmailPage = lazy(() => import('../features/auth/pages/VerifyEmailPage').then(m => ({ default: m.default || m.VerifyEmailPage })));
const ForgotPasswordPage = lazy(() => import('../features/auth/pages/ForgotPasswordPage').then(m => ({ default: m.default || m.ForgotPasswordPage })));
const ResetPasswordPage = lazy(() => import('../features/auth/pages/ResetPasswordPage').then(m => ({ default: m.default || m.ResetPasswordPage })));

const StockPage = lazy(() => import('../features/inventory/pages/StockPage').then(m => ({ default: m.default || m.StockPage })));
const FacilitiesPage = lazy(() => import('../features/inventory/pages/FacilitiesPage').then(m => ({ default: m.default || m.FacilitiesPage })));
const HospitalsPage = lazy(() => import('../features/hospitals/pages/HospitalsPage').then(m => ({ default: m.default || m.HospitalsPage })));
const HospitalStaffPage = lazy(() => import('../features/hospitals/pages/HospitalStaffPage').then(m => ({ default: m.default || m.HospitalStaffPage })));
const DonationsPage = lazy(() => import('../features/donations/pages/DonationsPage').then(m => ({ default: m.default || m.DonationsPage })));
const EligibilityPage = lazy(() => import('../features/donations/pages/EligibilityPage').then(m => ({ default: m.default || m.EligibilityPage })));
const RequestsPage = lazy(() => import('../features/requests/pages/RequestsPage').then(m => ({ default: m.default || m.RequestsPage })));
const UsersPage = lazy(() => import('../features/users/pages/UsersPage').then(m => ({ default: m.default || m.UsersPage })));
const RolesPage = lazy(() => import('../features/admin/pages/RolesPage').then(m => ({ default: m.default || m.RolesPage })));

const PageLoader: React.FC = () => (
  <div className="flex h-64 w-full items-center justify-center">
    <div className="flex flex-col items-center gap-2 text-rose-600">
      <Loader2 className="h-8 w-8 animate-spin" />
      <span className="text-xs font-medium text-slate-500">Loading module...</span>
    </div>
  </div>
);

// Route Guard Component: Requires Authentication
const ProtectedRoute: React.FC = () => {
  const { isAuthenticated } = useAuth();
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }
  return (
    <Suspense fallback={<PageLoader />}>
      <Outlet />
    </Suspense>
  );
};

// Route Guard Component: Unauthenticated Access Only (Redirects to landing if logged in)
const PublicOnlyRoute: React.FC = () => {
  const { isAuthenticated, getLandingRoute } = useAuth();
  if (isAuthenticated) {
    return <Navigate to={getLandingRoute()} replace />;
  }
  return (
    <Suspense fallback={<PageLoader />}>
      <Outlet />
    </Suspense>
  );
};

export const router = createBrowserRouter([
  {
    path: '/',
    element: <RootLayout />,
    errorElement: <NotFoundPage />,
    children: [
      {
        element: <PublicOnlyRoute />,
        children: [
          {
            path: 'login',
            element: <LoginPage />,
          },
          {
            path: 'register',
            element: <RegisterPage />,
          },
          {
            path: 'verify-email',
            element: <VerifyEmailPage />,
          },
          {
            path: 'forgot-password',
            element: <ForgotPasswordPage />,
          },
          {
            path: 'reset-password',
            element: <ResetPasswordPage />,
          },
          {
            index: true,
            element: <Navigate to="/login" replace />,
          },
        ],
      },
      {
        element: <ProtectedRoute />,
        children: [
          {
            element: <AdminLayout />,
            children: [
              {
                path: 'inventory',
                element: <StockPage />,
              },
              {
                path: 'facilities',
                element: <FacilitiesPage />,
              },
              {
                path: 'hospitals',
                element: <HospitalsPage />,
              },
              {
                path: 'hospitals/staff',
                element: <HospitalStaffPage />,
              },
              {
                path: 'donations',
                element: <DonationsPage />,
              },
              {
                path: 'donations/eligibility',
                element: <EligibilityPage />,
              },
              {
                path: 'requests',
                element: <RequestsPage />,
              },
              {
                path: 'users',
                element: <UsersPage />,
              },
              {
                path: 'admin/roles',
                element: <RolesPage />,
              },
            ],
          },
        ],
      },
    ],
  },
  {
    path: '*',
    element: <NotFoundPage />,
  },
]);
