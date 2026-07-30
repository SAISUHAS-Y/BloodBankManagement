import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router';
import { useForm } from 'react-hook-form';
import { DonorRegisterSchemaType, validateDonorAge } from '../schemas/authSchemas';
import { authService } from '../../../services/authService';
import { MasterApi, BloodGroupResponse } from '../../master/api/masterApi';
import { toast } from '../../../common/toast/ToastProvider';
import { AuthLayout } from '../components/AuthLayout';
import { FormInput } from '../components/FormInput';
import { PasswordStrengthMeter } from '../components/PasswordStrengthMeter';
import { Captcha } from '../components/Captcha';
import { Label } from '../../../shared/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../../../shared/components/ui/dialog';
import { Button } from '../../../shared/components/ui/button';
import {
  User,
  Mail,
  Phone,
  MapPin,
  Calendar,
  Lock,
  Droplet,
  UserPlus,
  Loader2,
  FileText,
  ArrowLeft,
  CheckCircle2,
} from 'lucide-react';

const FALLBACK_BLOOD_GROUPS: Partial<BloodGroupResponse>[] = [
  { code: 'UNKNOWN', displayName: 'Unknown / To be tested' },
  { code: 'O_POSITIVE', displayName: 'O+' },
  { code: 'O_NEGATIVE', displayName: 'O- (Universal Donor)' },
  { code: 'A_POSITIVE', displayName: 'A+' },
  { code: 'A_NEGATIVE', displayName: 'A-' },
  { code: 'B_POSITIVE', displayName: 'B+' },
  { code: 'B_NEGATIVE', displayName: 'B-' },
  { code: 'AB_POSITIVE', displayName: 'AB+ (Universal Recipient)' },
  { code: 'AB_NEGATIVE', displayName: 'AB-' },
];

export const RegisterPage: React.FC = () => {
  const navigate = useNavigate();
  const [botVerified, setBotVerified] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showPrivacyModal, setShowPrivacyModal] = useState(false);
  const [bloodGroups, setBloodGroups] = useState<Partial<BloodGroupResponse>[]>(FALLBACK_BLOOD_GROUPS);
  const [loadingMaster, setLoadingMaster] = useState(false);

  const {
    register,
    handleSubmit,
    watch,
    setValue,
    formState: { errors },
  } = useForm<DonorRegisterSchemaType>({
    defaultValues: {
      fullName: '',
      dateOfBirth: '',
      gender: 'MALE',
      bloodGroup: 'UNKNOWN',
      email: '',
      phone: '',
      address: '',
      city: '',
      username: '',
      password: '',
      confirmPassword: '',
      privacyConsent: true,
      dataConsent: true,
    },
  });

  const emailValue = watch('email');
  const usernameValue = watch('username');
  const passwordValue = watch('password');

  // Fetch MasterService Blood Groups dynamically
  useEffect(() => {
    let mounted = true;
    setLoadingMaster(true);
    MasterApi.getBloodGroups()
      .then((res) => {
        if (mounted && Array.isArray(res) && res.length > 0) {
          setBloodGroups(res);
        }
      })
      .catch((err) => {
        console.warn('MasterService lookup offline or unavailable, using fallback list:', err);
      })
      .finally(() => {
        if (mounted) setLoadingMaster(false);
      });

    return () => {
      mounted = false;
    };
  }, []);

  // Auto-suggest username from email prefix if empty
  const handleEmailBlur = () => {
    if (emailValue && !usernameValue) {
      const suggested = emailValue.split('@')[0].replace(/[^a-zA-Z0-9_]/g, '');
      if (suggested) {
        setValue('username', suggested, { shouldValidate: true });
      }
    }
  };

  const onSubmit = async (data: DonorRegisterSchemaType) => {
    if (!botVerified) {
      toast.error('Bot Protection', 'Please check the captcha verification before submitting.');
      return;
    }

    try {
      setIsSubmitting(true);
      await authService.registerDonor({
        fullName: data.fullName,
        dateOfBirth: data.dateOfBirth,
        gender: data.gender,
        bloodGroup: data.bloodGroup || 'UNKNOWN',
        email: data.email,
        phone: data.phone,
        address: data.address,
        city: data.city,
        username: data.username,
        password: data.password,
        confirmPassword: data.confirmPassword,
        privacyConsent: data.privacyConsent,
        dataConsent: data.dataConsent,
      });

      toast.success('Registration Complete', 'Donor account created! Check your email for OTP verification.');
      navigate(`/verify-email?email=${encodeURIComponent(data.email)}`);
    } catch (err: any) {
      toast.error('Registration Failed', err?.message || 'Failed to create donor account.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <AuthLayout
      title="Voluntary Donor Registration"
      subtitle="Join the Enterprise Blood Donor Registry & Save Lives"
      maxWidthClass="max-w-[640px]"
    >
      <form onSubmit={handleSubmit(onSubmit)} className="space-y-6 pt-4">
        {/* Section 1: Personal Info */}
        <div className="space-y-3 pb-2 border-b border-slate-800/80">
          <h3 className="text-xs font-bold text-rose-400 uppercase tracking-widest flex items-center gap-1.5">
            <User className="h-4 w-4" /> Personal Information
          </h3>

          <FormInput
            id="fullName"
            label="Full Name *"
            placeholder="e.g. Alex Mercer"
            icon={<User className="h-4 w-4" />}
            {...register('fullName', { required: 'Full Name is required' })}
            error={errors.fullName?.message}
            disabled={isSubmitting}
          />

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <FormInput
              id="dateOfBirth"
              label="Date of Birth (18-65 yrs) *"
              type="date"
              icon={<Calendar className="h-4 w-4" />}
              {...register('dateOfBirth', {
                required: 'Date of Birth is required',
                validate: validateDonorAge,
              })}
              error={errors.dateOfBirth?.message}
              disabled={isSubmitting}
            />

            <div className="space-y-1.5">
              <Label htmlFor="gender" className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Gender *
              </Label>
              <select
                id="gender"
                {...register('gender', { required: 'Gender is required' })}
                disabled={isSubmitting}
                className="w-full h-11 rounded-lg border border-slate-800 bg-slate-950/60 px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-rose-500"
              >
                <option value="MALE">Male</option>
                <option value="FEMALE">Female</option>
                <option value="OTHER">Other</option>
                <option value="PREFER_NOT_TO_SAY">Prefer not to say</option>
              </select>
              {errors.gender && <span className="text-[11px] text-red-400">{errors.gender.message}</span>}
            </div>

            <div className="space-y-1.5">
              <Label htmlFor="bloodGroup" className="text-xs font-semibold text-slate-300 uppercase tracking-wider flex items-center gap-1">
                <Droplet className="h-3 w-3 text-rose-500 fill-rose-600" /> Blood Group
              </Label>
              <select
                id="bloodGroup"
                {...register('bloodGroup')}
                disabled={isSubmitting || loadingMaster}
                className="w-full h-11 rounded-lg border border-slate-800 bg-slate-950/60 px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-rose-500"
              >
                {bloodGroups.map((bg) => (
                  <option key={bg.code} value={bg.code}>
                    {bg.displayName}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Section 2: Contact Info */}
        <div className="space-y-3 pb-2 border-b border-slate-800/80">
          <h3 className="text-xs font-bold text-rose-400 uppercase tracking-widest flex items-center gap-1.5">
            <Mail className="h-4 w-4" /> Contact & Location Details
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <FormInput
              id="email"
              label="Email Address *"
              type="email"
              placeholder="alex@example.com"
              icon={<Mail className="h-4 w-4" />}
              {...register('email', {
                required: 'Email is required',
                pattern: { value: /^\S+@\S+\.\S+$/, message: 'Invalid email address' },
              })}
              onBlur={handleEmailBlur}
              error={errors.email?.message}
              disabled={isSubmitting}
            />

            <FormInput
              id="phone"
              label="Phone Number (10 digits) *"
              placeholder="9876543210"
              icon={<Phone className="h-4 w-4" />}
              {...register('phone', {
                required: 'Phone number is required',
                pattern: { value: /^\d{10}$/, message: 'Phone number must be exactly 10 digits' },
              })}
              error={errors.phone?.message}
              disabled={isSubmitting}
            />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
            <div className="sm:col-span-2">
              <FormInput
                id="address"
                label="Street Address *"
                placeholder="123 Health Ave, Sector 4"
                icon={<MapPin className="h-4 w-4" />}
                {...register('address', { required: 'Street Address is required' })}
                error={errors.address?.message}
                disabled={isSubmitting}
              />
            </div>

            <FormInput
              id="city"
              label="City / District *"
              placeholder="Metropolis"
              icon={<MapPin className="h-4 w-4" />}
              {...register('city', { required: 'City is required' })}
              error={errors.city?.message}
              disabled={isSubmitting}
            />
          </div>
        </div>

        {/* Section 3: Account Credentials */}
        <div className="space-y-3 pb-2 border-b border-slate-800/80">
          <h3 className="text-xs font-bold text-rose-400 uppercase tracking-widest flex items-center gap-1.5">
            <Lock className="h-4 w-4" /> Account Credentials
          </h3>

          <FormInput
            id="username"
            label="Username *"
            placeholder="alex_mercer"
            icon={<User className="h-4 w-4" />}
            {...register('username', {
              required: 'Username is required',
              minLength: { value: 3, message: 'Username must be at least 3 characters' },
            })}
            error={errors.username?.message}
            disabled={isSubmitting}
          />

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div className="space-y-1">
              <FormInput
                id="password"
                label="Password (min 8 chars) *"
                type="password"
                placeholder="••••••••"
                icon={<Lock className="h-4 w-4" />}
                {...register('password', {
                  required: 'Password is required',
                  minLength: { value: 8, message: 'Password must be at least 8 characters' },
                })}
                error={errors.password?.message}
                disabled={isSubmitting}
              />
              <PasswordStrengthMeter password={passwordValue} />
            </div>

            <FormInput
              id="confirmPassword"
              label="Confirm Password *"
              type="password"
              placeholder="••••••••"
              icon={<Lock className="h-4 w-4" />}
              {...register('confirmPassword', {
                required: 'Please confirm password',
                validate: (val) => val === passwordValue || 'Passwords do not match',
              })}
              error={errors.confirmPassword?.message}
              disabled={isSubmitting}
            />
          </div>
        </div>

        {/* Section 4: Consent & Captcha */}
        <div className="space-y-3 pt-1">
          <div className="space-y-2 text-xs">
            <label className="flex items-start gap-2.5 text-slate-300 cursor-pointer select-none">
              <input
                type="checkbox"
                {...register('privacyConsent', { required: 'You must confirm info accuracy' })}
                className="mt-0.5 accent-rose-600 rounded"
              />
              <span>
                I confirm the information provided above is complete and accurate. *
              </span>
            </label>

            <label className="flex items-start gap-2.5 text-slate-300 cursor-pointer select-none">
              <input
                type="checkbox"
                {...register('dataConsent', { required: 'You must consent to data processing' })}
                className="mt-0.5 accent-rose-600 rounded"
              />
              <span>
                I consent to my medical and donation history being processed according to the{' '}
                <button
                  type="button"
                  onClick={() => setShowPrivacyModal(true)}
                  className="text-rose-400 hover:text-rose-300 underline font-semibold"
                >
                  Privacy Policy & Terms of Service
                </button>
                . *
              </span>
            </label>
          </div>

          <Captcha verified={botVerified} onChange={setBotVerified} />
        </div>

        {/* Action Button */}
        <div className="pt-2">
          <button
            type="submit"
            disabled={isSubmitting}
            className="w-full bg-rose-600 hover:bg-rose-700 active:scale-[0.99] text-white font-semibold py-3.5 rounded-lg shadow-lg shadow-rose-950/60 transition-all duration-200 flex items-center justify-center gap-2 h-12 disabled:opacity-50"
          >
            {isSubmitting ? (
              <>
                <Loader2 className="h-5 w-5 animate-spin" />
                <span>Creating Donor Account...</span>
              </>
            ) : (
              <>
                <UserPlus className="h-5 w-5" />
                <span>Create Donor Account</span>
              </>
            )}
          </button>
        </div>

        {/* Back to Login Link */}
        <div className="text-center pt-2">
          <p className="text-xs text-slate-400">
            Already have an account?{' '}
            <Link
              to="/login"
              className="text-rose-400 hover:text-rose-300 font-semibold inline-flex items-center gap-1 transition-colors"
            >
              <ArrowLeft className="h-3.5 w-3.5" /> Back to Sign In
            </Link>
          </p>
        </div>
      </form>

      {/* Privacy Policy Modal */}
      <Dialog open={showPrivacyModal} onOpenChange={setShowPrivacyModal}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <FileText className="h-5 w-5 text-rose-600" /> Terms of Service & Privacy Policy
            </DialogTitle>
            <DialogDescription>
              Blood Bank Management System Privacy & Data Protection Agreement
            </DialogDescription>
          </DialogHeader>

          <div className="space-y-3 text-xs text-slate-300 max-h-60 overflow-y-auto py-2 pr-2">
            <p>
              1. <strong>Data Collection:</strong> We collect personal details, blood group, contact information, and donation vitals exclusively for blood bank inventory management and emergency dispatch.
            </p>
            <p>
              2. <strong>Medical Privacy:</strong> Your blood test results and eligibility screening details are confidential and accessible only by authorized medical staff.
            </p>
            <p>
              3. <strong>Emergency Contact:</strong> Registered donors may receive notifications when an urgent blood request matches their blood group in their designated city.
            </p>
          </div>

          <DialogFooter className="pt-2">
            <Button
              type="button"
              className="bg-rose-600 hover:bg-rose-700 text-white"
              onClick={() => setShowPrivacyModal(false)}
            >
              <CheckCircle2 className="h-4 w-4 mr-1" /> I Accept Terms
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </AuthLayout>
  );
};

export default RegisterPage;
