import { REGEX_PATTERNS } from './regex';

export const validators = {
  required: (value: unknown, fieldName: string = 'Field'): string | null => {
    if (value === undefined || value === null || (typeof value === 'string' && value.trim() === '')) {
      return `${fieldName} is required.`;
    }
    return null;
  },

  email: (value: string): string | null => {
    if (!value) return null;
    return REGEX_PATTERNS.EMAIL.test(value) ? null : 'Please enter a valid email address.';
  },

  phone: (value: string): string | null => {
    if (!value) return null;
    return REGEX_PATTERNS.PHONE.test(value) ? null : 'Please enter a valid phone number (e.g. +1234567890).';
  },

  bloodGroup: (value: string): string | null => {
    if (!value) return null;
    return REGEX_PATTERNS.BLOOD_GROUP.test(value) ? null : 'Valid blood groups are A+, A-, B+, B-, AB+, AB-, O+, O-.';
  },

  positiveNumber: (value: number | string, fieldName: string = 'Value'): string | null => {
    const num = Number(value);
    if (isNaN(num) || num <= 0) {
      return `${fieldName} must be a positive number.`;
    }
    return null;
  },
};
