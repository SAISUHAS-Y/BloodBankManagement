import { useState, useCallback } from 'react';

export interface ValidationRules<T> {
  [key: string]: (value: any, formData?: T) => string | null;
}

export function useValidation<T extends Record<string, any>>(rules: ValidationRules<T>) {
  const [errors, setErrors] = useState<Record<string, string>>({});

  const validate = useCallback(
    (formData: T): boolean => {
      const newErrors: Record<string, string> = {};
      let isValid = true;

      for (const fieldName in rules) {
        const ruleFn = rules[fieldName];
        if (ruleFn) {
          const errorMsg = ruleFn(formData[fieldName], formData);
          if (errorMsg) {
            newErrors[fieldName] = errorMsg;
            isValid = false;
          }
        }
      }

      setErrors(newErrors);
      return isValid;
    },
    [rules]
  );

  const clearFieldError = useCallback((fieldName: string) => {
    setErrors((prev) => {
      const copy = { ...prev };
      delete copy[fieldName];
      return copy;
    });
  }, []);

  const clearAllErrors = useCallback(() => {
    setErrors({});
  }, []);

  return {
    errors,
    validate,
    clearFieldError,
    clearAllErrors,
    setErrors,
  };
}
