import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router';
import { useAuth } from '../context/AuthContext';
import { authService } from '../../../services/authService';
import { toast } from '../../../common/toast/ToastProvider';
import { AuthLayout } from '../components/AuthLayout';
import { FormInput } from '../components/FormInput';
import { Captcha } from '../components/Captcha';
import { DemoAccountCard } from '../../../components/DemoAccountCard';
import {
  User,
  Lock,
  LogIn,
  Eye,
  EyeOff,
  CheckSquare,
  Square,
  Loader2,
  AlertCircle,
  UserPlus,
} from 'lucide-react';

export interface LoginPageProps {
  onLogin?: (identifier: string, password: string) => Promise<void>;
}

export const LoginPage: React.FC<LoginPageProps> = ({ onLogin }) => {
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  // Form state
  const [identifier, setIdentifier] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [botVerified, setBotVerified] = useState(false);

  // Status & Lockout
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ identifier?: string; password?: string }>({});
  const [failedAttempts, setFailedAttempts] = useState(0);
  const [lockoutSeconds, setLockoutSeconds] = useState(0);

  // Check URL query parameters for session expiry
  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    if (searchParams.get('expired') === 'true') {
      toast.warning('Session Expired', 'Please log in again to continue your session.');
    }
  }, [location.search]);

  // Lockout Countdown
  useEffect(() => {
    if (lockoutSeconds <= 0) return;
    const timer = setInterval(() => {
      setLockoutSeconds((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [lockoutSeconds]);

  const validateForm = (): boolean => {
    const errs: { identifier?: string; password?: string } = {};
    if (!identifier.trim()) {
      errs.identifier = 'Username, Email, or Phone is required';
    }
    if (!password.trim()) {
      errs.password = 'Password is required';
    }
    setFieldErrors(errs);
    return Object.keys(errs).length === 0;
  };

  const getRoleLandingRoute = (roles: string[] = []): string => {
    const uppercaseRoles = roles.map((r) => r.toUpperCase());
    if (uppercaseRoles.includes('ADMIN') || uppercaseRoles.includes('ROLE_ADMIN')) {
      return '/admin/dashboard';
    }
    if (uppercaseRoles.includes('HOSPITAL_STAFF') || uppercaseRoles.includes('ROLE_HOSPITAL_STAFF')) {
      return '/hospital/dashboard';
    }
    if (uppercaseRoles.includes('BANK_STAFF') || uppercaseRoles.includes('ROLE_BANK_STAFF') || uppercaseRoles.includes('BLOOD_BANK_STAFF')) {
      return '/bank/dashboard';
    }
    if (uppercaseRoles.includes('DONOR') || uppercaseRoles.includes('ROLE_DONOR') || uppercaseRoles.includes('DONOR_USER')) {
      return '/donor/dashboard';
    }
    return '/hospitals';
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (lockoutSeconds > 0) return;

    if (!validateForm()) {
      setErrorMsg('Please complete all required fields.');
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMsg(null);

      if (onLogin) {
        await onLogin(identifier, password);
      } else {
        const response = await authService.login({ identifier, password });
        if (response?.user) {
          toast.success('Welcome back', `Logged in as ${response.user.username}`);
          const target = getRoleLandingRoute(response.user.roles);
          const fromPath = (location.state as any)?.from?.pathname;
          navigate(fromPath || target, { replace: true });
          return;
        }
      }

      toast.success('Welcome back', 'Login successful.');
      navigate('/hospitals', { replace: true });
    } catch (err: any) {
      const newAttempts = failedAttempts + 1;
      setFailedAttempts(newAttempts);

      if (newAttempts >= 5) {
        setLockoutSeconds(900); // 15 minutes lockout
        setFailedAttempts(0);
        const msg = 'Account locked for 15 minutes due to repeated failed attempts.';
        setErrorMsg(msg);
        toast.error('Account Lockout', msg);
      } else {
        const msg = err?.message || 'Invalid username or password.';
        setErrorMsg(msg);
        toast.error('Authentication Failed', msg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  const fillDemoCredentials = (demoUsername: string) => {
    setIdentifier(demoUsername);
    setPassword('Password123!');
    setFieldErrors({});
    setErrorMsg(null);
  };

  return (
    <AuthLayout
      title="Blood Bank Management"
      subtitle="Enterprise Blood Donation & Allocation Platform"
    >
      {/* Lockout Warning */}
      {lockoutSeconds > 0 && (
        <div className="mt-4 flex items-center gap-2.5 rounded-xl bg-amber-950/70 border border-amber-700/60 p-3.5 text-xs text-amber-200 animate-fade-in">
          <AlertCircle className="h-5 w-5 shrink-0 text-amber-400 animate-bounce" />
          <div>
            <p className="font-semibold">Account Security Lockout</p>
            <p>Account locked for 15 minutes ({Math.floor(lockoutSeconds / 60)}m {lockoutSeconds % 60}s remaining).</p>
          </div>
        </div>
      )}

      {/* Error Alert */}
      {errorMsg && lockoutSeconds <= 0 && (
        <div className="mt-4 flex items-center gap-2.5 rounded-xl bg-red-950/70 border border-red-800/60 p-3.5 text-xs text-red-200 animate-fade-in">
          <AlertCircle className="h-5 w-5 shrink-0 text-red-400" />
          <span>{errorMsg}</span>
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-4 pt-4">
        {/* Identifier Field */}
        <FormInput
          id="identifier"
          name="identifier"
          label="Username, Email, or Phone"
          placeholder="Enter username, email, or 10-digit phone"
          autoComplete="username"
          icon={<User className="h-4 w-4" />}
          value={identifier}
          onChange={(e) => setIdentifier(e.target.value)}
          error={fieldErrors.identifier}
          disabled={lockoutSeconds > 0 || isSubmitting}
          required
        />

        {/* Password Field */}
        <div className="space-y-1.5">
          <div className="flex justify-between items-center">
            <span className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
              Password
            </span>
            <Link
              to="/forgot-password"
              className="text-xs text-rose-400 hover:text-rose-300 font-medium transition-colors"
            >
              Forgot password?
            </Link>
          </div>
          <FormInput
            id="password"
            name="password"
            label=""
            type={showPassword ? 'text' : 'password'}
            placeholder="••••••••"
            autoComplete="current-password"
            icon={<Lock className="h-4 w-4" />}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            error={fieldErrors.password}
            disabled={lockoutSeconds > 0 || isSubmitting}
            rightElement={
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                className="text-slate-500 hover:text-slate-300 focus:outline-none"
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
              </button>
            }
            required
          />
        </div>

        {/* Remember Me & Captcha */}
        <div className="space-y-3 pt-1">
          <div className="flex items-center justify-between text-xs">
            <label className="flex items-center gap-2 text-slate-300 cursor-pointer select-none">
              <button
                type="button"
                role="checkbox"
                aria-checked={rememberMe}
                onClick={() => setRememberMe(!rememberMe)}
                className="text-rose-500 focus:outline-none"
              >
                {rememberMe ? <CheckSquare className="h-4 w-4 fill-rose-600 text-slate-900" /> : <Square className="h-4 w-4 text-slate-600" />}
              </button>
              <span>Remember me</span>
            </label>
          </div>

          <Captcha verified={botVerified} onChange={setBotVerified} />
        </div>

        {/* Quick Demo Accounts Grid */}
        <div className="pt-2">
          <span className="text-[10px] font-bold text-slate-400 uppercase tracking-widest block mb-2">
            QUICK DEMO ACCOUNTS
          </span>
          <div className="grid grid-cols-2 gap-2 text-xs">
            <DemoAccountCard
              roleLabel="Admin"
              username="admin"
              colorScheme="red"
              onSelect={fillDemoCredentials}
            />
            <DemoAccountCard
              roleLabel="Hospital Staff"
              username="hospital_staff"
              colorScheme="blue"
              onSelect={fillDemoCredentials}
            />
            <DemoAccountCard
              roleLabel="Blood Bank Staff"
              username="bank_staff"
              colorScheme="green"
              onSelect={fillDemoCredentials}
            />
            <DemoAccountCard
              roleLabel="Donor"
              username="donor_user"
              colorScheme="amber"
              onSelect={fillDemoCredentials}
            />
          </div>
        </div>

        {/* Sign In Button */}
        <div className="pt-2">
          <button
            type="submit"
            disabled={isSubmitting || lockoutSeconds > 0 || !identifier || !password}
            className="w-full bg-rose-600 hover:bg-rose-700 active:scale-[0.99] text-white font-semibold py-3 rounded-lg shadow-lg shadow-rose-950/60 transition-all duration-200 flex items-center justify-center gap-2 h-11 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                <span>Signing in...</span>
              </>
            ) : (
              <>
                <LogIn className="h-4 w-4" />
                <span>Sign In</span>
              </>
            )}
          </button>
        </div>

        {/* Create Donor Account Link */}
        <div className="text-center pt-3 border-t border-slate-800/80">
          <p className="text-xs text-slate-400">
            New donor?{' '}
            <Link
              to="/register"
              className="text-rose-400 hover:text-rose-300 font-semibold inline-flex items-center gap-1 transition-colors"
            >
              <UserPlus className="h-3.5 w-3.5" /> Create an account
            </Link>
          </p>
        </div>
      </form>
    </AuthLayout>
  );
};

export default LoginPage;
