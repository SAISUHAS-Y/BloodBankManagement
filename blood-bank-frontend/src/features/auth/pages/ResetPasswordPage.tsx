import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router';
import { authService } from '../../../services/authService';
import { toast } from '../../../common/toast/ToastProvider';
import { AuthLayout } from '../components/AuthLayout';
import { FormInput } from '../components/FormInput';
import { PasswordStrengthMeter } from '../components/PasswordStrengthMeter';
import { Button } from '../../../shared/components/ui/button';
import { Lock, ArrowLeft, Loader2, KeyRound } from 'lucide-react';

export const ResetPasswordPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword.length < 8) {
      setErrorMsg('Password must be at least 8 characters long.');
      return;
    }
    if (newPassword !== confirmPassword) {
      setErrorMsg('Passwords do not match.');
      return;
    }

    const searchParams = new URLSearchParams(location.search);
    const token = searchParams.get('token') || 'demo-reset-token';

    try {
      setIsSubmitting(true);
      setErrorMsg(null);
      await authService.resetPassword({ token, newPassword });
      toast.success('Password Updated', 'Your password has been reset. Please sign in.');
      navigate('/login');
    } catch (err: any) {
      toast.error('Reset Failed', err?.message || 'Password reset link is invalid or expired.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Set New Password"
      subtitle="Enter a strong new password for your account"
    >
      <form onSubmit={handleSubmit} className="space-y-4 pt-4">
        {errorMsg && (
          <div className="flex items-center gap-2 rounded-xl bg-red-950/70 border border-red-800/60 p-3.5 text-xs text-red-200">
            <span>{errorMsg}</span>
          </div>
        )}

        <div className="space-y-1">
          <FormInput
            id="newPassword"
            label="New Password *"
            type="password"
            placeholder="••••••••"
            icon={<Lock className="h-4 w-4" />}
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            disabled={isSubmitting}
            required
          />
          <PasswordStrengthMeter password={newPassword} />
        </div>

        <FormInput
          id="confirmPassword"
          label="Confirm New Password *"
          type="password"
          placeholder="••••••••"
          icon={<Lock className="h-4 w-4" />}
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          disabled={isSubmitting}
          required
        />

        <Button
          type="submit"
          disabled={isSubmitting}
          className="w-full bg-rose-600 hover:bg-rose-700 text-white font-semibold py-3.5 rounded-lg shadow-lg shadow-rose-950/50 transition-all flex items-center justify-center gap-2 h-11"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Updating Password...</span>
            </>
          ) : (
            <>
              <KeyRound className="h-4 w-4" />
              <span>Update Password</span>
            </>
          )}
        </Button>

        <div className="text-center pt-2 border-t border-slate-800/80">
          <Link
            to="/login"
            className="text-xs text-slate-400 hover:text-slate-200 inline-flex items-center gap-1 transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Back to Sign In
          </Link>
        </div>
      </form>
    </AuthLayout>
  );
};

export default ResetPasswordPage;
