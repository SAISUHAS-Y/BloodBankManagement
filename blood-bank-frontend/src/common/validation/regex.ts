export const REGEX_PATTERNS = {
  EMAIL: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
  PHONE: /^\+?[1-9]\d{1,14}$/, // E.164 international phone number
  BLOOD_GROUP: /^(A|B|AB|O)[+-]$/,
  HOSPITAL_CODE: /^HOSP-[A-Z0-9]{4,8}$/,
  BLOOD_BANK_CODE: /^BB-[A-Z0-9]{4,8}$/,
  POSITIVE_INTEGER: /^[1-9]\d*$/,
  NON_NEGATIVE_INTEGER: /^(0|[1-9]\d*)$/,
};
