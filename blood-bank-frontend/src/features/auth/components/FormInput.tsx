import React, { forwardRef } from 'react';
import { Label } from '../../../shared/components/ui/label';
import { Input } from '../../../shared/components/ui/input';

export interface FormInputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label: string;
  id: string;
  icon?: React.ReactNode;
  error?: string;
  rightElement?: React.ReactNode;
}

export const FormInput = forwardRef<HTMLInputElement, FormInputProps>(
  ({ label, id, icon, error, rightElement, className = '', ...props }, ref) => {
    return (
      <div className="space-y-1.5 w-full">
        {label && (
          <Label htmlFor={id} className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
            {label}
          </Label>
        )}
        <div className="relative">
          {icon && (
            <div className="absolute left-3.5 top-3.5 text-slate-500 pointer-events-none">
              {icon}
            </div>
          )}
          <Input
            ref={ref}
            id={id}
            aria-invalid={!!error}
            className={`${icon ? 'pl-10' : 'pl-3.5'} ${rightElement ? 'pr-10' : 'pr-3.5'} bg-slate-950/60 border-slate-800 text-white placeholder:text-slate-500 focus-visible:ring-rose-500 h-11 ${
              error ? 'border-red-600 ring-1 ring-red-600' : ''
            } ${className}`}
            {...props}
          />
          {rightElement && (
            <div className="absolute right-3.5 top-3.5 flex items-center">
              {rightElement}
            </div>
          )}
        </div>
        {error && <span className="text-[11px] text-red-400 font-medium">{error}</span>}
      </div>
    );
  }
);

FormInput.displayName = 'FormInput';

export default FormInput;
