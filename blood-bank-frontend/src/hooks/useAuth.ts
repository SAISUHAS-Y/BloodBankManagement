import { useAuth as useAuthContext } from '../features/auth/context/AuthContext';

/**
 * Custom authentication hook for accessing auth state, token management,
 * and role-based access control.
 */
export const useAuth = () => {
  return useAuthContext();
};

export default useAuth;
