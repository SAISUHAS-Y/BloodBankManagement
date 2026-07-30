import React, { useState } from 'react';
import { Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { ForgotPasswordSchemaType } from '../schemas/authSchemas';
import { authService } from '../../../services/authService';
import { toast } from '../../../common/toast/ToastProvider';
import { AuthLayout } from '../components/AuthLayout';
import { FormInput } from '../components/FormInput';
import { Button } from '../../../shared/components/ui/button';
import { Mail, ArrowLeft, Send, Loader2 } from 'lucide-react';

export const ForgotPasswordPage: React.FC = () => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm<ForgotPasswordSchemaType>({
    defaultValues: { identifier: '' },
  });

  const identifier = watch('identifier');

  const onSubmit = async (data: ForgotPasswordSchemaType) => {
    try {
      setIsSubmitting(true);
      await authService.forgotPassword(data.identifier);
      setSubmitted(true);
      toast.success('Reset Link Sent', 'Instructions have been dispatched to your email.');
    } catch (err: any) {
      toast.error('Request Failed', err?.message || 'Unable to process password reset request.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Reset Your Password"
      subtitle="Enter your email or username to receive recovery instructions"
    >
      {submitted ? (
        <div className="text-center space-y-4 py-4">
          <div className="inline-flex h-12 w-12 items-center justify-center rounded-full bg-emerald-950 text-emerald-400">
            <Send className="h-6 w-6" />
          </div>
          <h3 className="text-base font-bold text-white">Recovery Instructions Sent</h3>
          <p className="text-xs text-slate-400">
            If an account exists for <span className="text-slate-200 font-semibold">{identifier}</span>, you will receive an email with a secure reset link shortly.
          </p>
          <div className="pt-2">
            <Link
              to="/login"
              className="inline-flex items-center gap-1.5 text-xs text-rose-400 hover:text-rose-300 font-semibold"
            >
              <ArrowLeft className="h-4 w-4" /> Return to Login
            </Link>
          </div>
        </div>
      ) : (
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-5 pt-4">
          <FormInput
            id="identifier"
            label="Registered Email or Username *"
            placeholder="e.g. alex@example.com or admin"
            icon={<Mail className="h-4 w-4" />}
            {...register('identifier', { required: 'Please enter registered email or username' })}
            error={errors.identifier?.message}
            disabled={isSubmitting}
            autoFocus
          />

          <Button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-rose-600 hover:bg-rose-700 text-white font-semibold py-3.5 rounded-lg shadow-lg shadow-rose-950/50 transition-all flex items-center justify-center gap-2 h-11"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="h-4 w-4 animate-spin" />
                <span>Sending Reset Link...</span>
              </>
            ) : (
              <>
                <Send className="h-4 w-4" />
                <span>Send Reset Link</span>
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
      )}
    </AuthLayout>
  );
};

export default ForgotPasswordPage;
