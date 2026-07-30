import React, { useState, useEffect } from 'react';
import { useNavigate, useLocation, Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { OtpSchemaType } from '../schemas/authSchemas';
import { authService } from '../../../services/authService';
import { toast } from '../../../common/toast/ToastProvider';
import { AuthLayout } from '../components/AuthLayout';
import { Input } from '../../../shared/components/ui/input';
import { Label } from '../../../shared/components/ui/label';
import { Button } from '../../../shared/components/ui/button';
import { KeyRound, ShieldCheck, RefreshCw, ArrowLeft, Loader2 } from 'lucide-react';

export const VerifyEmailPage: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [resendCooldown, setResendCooldown] = useState(30);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const {
    register,
    handleSubmit,
    setValue,
    watch,
    formState: { errors },
  } = useForm<OtpSchemaType>({
    defaultValues: { otpCode: '' },
  });

  const otpCode = watch('otpCode');

  useEffect(() => {
    const searchParams = new URLSearchParams(location.search);
    const emailParam = searchParams.get('email') || 'donor@bloodbank.org';
    setEmail(emailParam);
  }, [location.search]);

  // Resend Timer
  useEffect(() => {
    if (resendCooldown <= 0) return;
    const timer = setInterval(() => {
      setResendCooldown((prev) => prev - 1);
    }, 1000);
    return () => clearInterval(timer);
  }, [resendCooldown]);

  const onSubmit = async (data: OtpSchemaType) => {
    try {
      setIsSubmitting(true);
      await authService.verifyOtp({ email, otpCode: data.otpCode });
      toast.success('Account Verified!', 'Your email has been verified. You can now log in.');
      navigate('/login');
    } catch (err: any) {
      toast.error('Verification Failed', err?.message || 'Invalid or expired OTP code.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleResend = () => {
    setResendCooldown(30);
    toast.success('Code Sent', `A new 6-digit OTP verification code was sent to ${email}`);
  };

  return (
    <AuthLayout
      title="Verify Your Email"
      subtitle={`Enter the 6-digit code sent to ${email}`}
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 pt-4">
        <div className="text-center space-y-1 pb-2">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-2xl bg-rose-950/80 border border-rose-600/40 text-rose-400 mb-1">
            <KeyRound className="h-6 w-6" />
          </div>
          <p className="text-xs text-slate-400">
            Check your inbox and enter the single-use authorization code below.
          </p>
        </div>

        <div className="space-y-2">
          <Label htmlFor="otpCode" className="text-xs font-semibold text-slate-300 uppercase tracking-wider block text-center">
            6-Digit Verification Code
          </Label>
          <Input
            id="otpCode"
            maxLength={6}
            placeholder="123456"
            {...register('otpCode', {
              required: 'OTP code is required',
              minLength: { value: 6, message: 'OTP must be exactly 6 digits' },
            })}
            onChange={(e) => setValue('otpCode', e.target.value.replace(/\D/g, ''))}
            className="text-center font-mono text-xl tracking-[0.6em] bg-slate-950/60 border-slate-800 text-white focus-visible:ring-rose-500 h-14"
            disabled={isSubmitting}
            autoFocus
          />
          {errors.otpCode && (
            <p className="text-center text-[11px] text-red-400 font-medium">{errors.otpCode.message}</p>
          )}
        </div>

        <Button
          type="submit"
          disabled={isSubmitting || (otpCode?.length || 0) < 6}
          className="w-full bg-rose-600 hover:bg-rose-700 text-white font-semibold py-3.5 rounded-lg shadow-lg shadow-rose-950/50 transition-all flex items-center justify-center gap-2 h-11"
        >
          {isSubmitting ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              <span>Verifying Code...</span>
            </>
          ) : (
            <>
              <ShieldCheck className="h-4 w-4" />
              <span>Verify & Activate Account</span>
            </>
          )}
        </Button>

        <div className="flex items-center justify-between text-xs pt-2 border-t border-slate-800/80">
          <Link
            to="/login"
            className="text-slate-400 hover:text-slate-200 flex items-center gap-1 transition-colors"
          >
            <ArrowLeft className="h-3.5 w-3.5" /> Back to Sign In
          </Link>

          <button
            type="button"
            onClick={handleResend}
            disabled={resendCooldown > 0}
            className="text-rose-400 hover:text-rose-300 font-semibold disabled:text-slate-600 flex items-center gap-1 transition-colors"
          >
            <RefreshCw className={`h-3.5 w-3.5 ${resendCooldown > 0 ? 'animate-spin' : ''}`} />
            {resendCooldown > 0 ? `Resend in ${resendCooldown}s` : 'Resend Code'}
          </button>
        </div>
      </form>
    </AuthLayout>
  );
};

export default VerifyEmailPage;
