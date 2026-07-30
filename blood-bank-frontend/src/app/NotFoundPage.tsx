import React from 'react';
import { useNavigate, useRouteError } from 'react-router';
import { Button } from '../shared/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../shared/components/ui/card';
import { AlertCircle, Home, ArrowLeft } from 'lucide-react';

export const NotFoundPage: React.FC = () => {
  const navigate = useNavigate();
  const error: any = useRouteError();

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <Card className="max-w-md w-full bg-slate-900 border-slate-800 text-slate-100 shadow-2xl text-center">
        <CardHeader className="space-y-3">
          <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-red-950/60 border border-red-800 text-red-500">
            <AlertCircle className="h-8 w-8" />
          </div>
          <CardTitle className="text-2xl font-bold tracking-tight">
            {error ? 'An Error Occurred' : 'Page Not Found'}
          </CardTitle>
          <CardDescription className="text-slate-400 text-sm">
            {error?.statusText || error?.message || "The page or resource you requested could not be found."}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col sm:flex-row gap-3 pt-2">
          <Button
            variant="outline"
            onClick={() => navigate(-1)}
            className="flex-1 border-slate-700 text-slate-300 hover:bg-slate-800"
          >
            <ArrowLeft className="h-4 w-4 mr-2" /> Go Back
          </Button>
          <Button
            onClick={() => navigate('/inventory')}
            className="flex-1 bg-rose-600 hover:bg-rose-700 text-white"
          >
            <Home className="h-4 w-4 mr-2" /> Dashboard
          </Button>
        </CardContent>
      </Card>
    </div>
  );
};
