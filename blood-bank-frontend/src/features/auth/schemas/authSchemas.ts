export interface LoginSchemaType {
  identifier: string;
  password: string;
}

export interface DonorRegisterSchemaType {
  fullName: string;
  dateOfBirth: string;
  gender: string;
  bloodGroup?: string;
  email: string;
  phone: string;
  address: string;
  city: string;
  username: string;
  password: string;
  confirmPassword: string;
  privacyConsent: boolean;
  dataConsent: boolean;
}

export interface OtpSchemaType {
  otpCode: string;
}

export interface ForgotPasswordSchemaType {
  identifier: string;
}

/**
 * Validates that the donor is between 18 and 65 years old.
 */
export const validateDonorAge = (dob: string): true | string => {
  if (!dob) return 'Date of Birth is required';
  const birthDate = new Date(dob);
  if (isNaN(birthDate.getTime())) return 'Invalid date format';

  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const m = today.getMonth() - birthDate.getMonth();
  if (m < 0 || (m === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }

  if (age < 18 || age > 65) {
    return 'Donor must be between 18 and 65 years old';
  }
  return true;
};
