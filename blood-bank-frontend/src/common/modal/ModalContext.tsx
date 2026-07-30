import React, { createContext, useContext, useState, useCallback } from 'react';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../../shared/components/ui/dialog';
import { Button } from '../../shared/components/ui/button';
import { AlertTriangle, Info, CheckCircle2 } from 'lucide-react';

interface ConfirmOptions {
  title: string;
  message: string | React.ReactNode;
  confirmText?: string;
  cancelText?: string;
  variant?: 'danger' | 'warning' | 'info' | 'success';
  onConfirm: () => void | Promise<void>;
}

interface ModalContextType {
  confirm: (options: ConfirmOptions) => void;
  closeModal: () => void;
}

const ModalContext = createContext<ModalContextType | undefined>(undefined);

export const ModalProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [modalState, setModalState] = useState<ConfirmOptions | null>(null);
  const [isOpen, setIsOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const confirm = useCallback((options: ConfirmOptions) => {
    setModalState(options);
    setIsOpen(true);
  }, []);

  const closeModal = useCallback(() => {
    if (isSubmitting) return;
    setIsOpen(false);
    setModalState(null);
  }, [isSubmitting]);

  const handleConfirm = async () => {
    if (!modalState) return;
    try {
      setIsSubmitting(true);
      await modalState.onConfirm();
      setIsOpen(false);
      setModalState(null);
    } catch (err) {
      console.error('Modal action error:', err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const getIcon = () => {
    switch (modalState?.variant) {
      case 'danger':
        return <AlertTriangle className="h-6 w-6 text-red-600" />;
      case 'warning':
        return <AlertTriangle className="h-6 w-6 text-amber-500" />;
      case 'success':
        return <CheckCircle2 className="h-6 w-6 text-emerald-600" />;
      default:
        return <Info className="h-6 w-6 text-blue-600" />;
    }
  };

  const getButtonVariant = () => {
    switch (modalState?.variant) {
      case 'danger':
        return 'destructive' as const;
      default:
        return 'default' as const;
    }
  };

  return (
    <ModalContext.Provider value={{ confirm, closeModal }}>
      {children}
      <Dialog open={isOpen} onOpenChange={closeModal}>
        <DialogContent className="sm:max-w-[425px]">
          {modalState && (
            <>
              <DialogHeader className="flex flex-row items-center gap-3 space-y-0">
                <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-800">
                  {getIcon()}
                </div>
                <div className="flex flex-col">
                  <DialogTitle>{modalState.title}</DialogTitle>
                </div>
              </DialogHeader>
              <DialogDescription className="py-2 text-slate-600 dark:text-slate-400">
                {modalState.message}
              </DialogDescription>
              <DialogFooter className="gap-2 sm:gap-0">
                <Button
                  type="button"
                  variant="outline"
                  onClick={closeModal}
                  disabled={isSubmitting}
                >
                  {modalState.cancelText || 'Cancel'}
                </Button>
                <Button
                  type="button"
                  variant={getButtonVariant()}
                  onClick={handleConfirm}
                  disabled={isSubmitting}
                >
                  {isSubmitting ? 'Processing...' : modalState.confirmText || 'Confirm'}
                </Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>
    </ModalContext.Provider>
  );
};

export const useModal = (): ModalContextType => {
  const context = useContext(ModalContext);
  if (!context) {
    throw new Error('useModal must be used within a ModalProvider');
  }
  return context;
};
