import React, { useState } from 'react';
import { DonationApi, EligibilityRequest, EligibilityResponse } from '../api/donationApi';
import { toast } from '../../../common/toast/ToastProvider';
import { Button } from '../../../shared/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../../../shared/components/ui/card';
import { Input } from '../../../shared/components/ui/input';
import { Label } from '../../../shared/components/ui/label';
import { Badge } from '../../../shared/components/ui/badge';
import { Stethoscope, CheckCircle2, XCircle, ArrowLeft, HeartPulse } from 'lucide-react';
import { useNavigate } from 'react-router';

export const EligibilityPage: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState<EligibilityResponse | null>(null);

  const [formData, setFormData] = useState<EligibilityRequest>({
    donorProfileId: 1,
    weightKg: 65,
    hemoglobinGdl: 14.2,
    pulseBpm: 72,
    systolicBp: 120,
    diastolicBp: 80,
    temperatureCelsius: 36.8,
  });

  const handleScreening = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      setLoading(true);
      const res = await DonationApi.checkEligibility(formData);
      setResult(res);
      if (res.eligible) {
        toast.success('Donor Eligible', 'Donor meets all physiological criteria for blood donation.');
      } else {
        toast.warning('Donor Ineligible', 'Donor does not meet minimum safety thresholds.');
      }
    } catch (err: any) {
      toast.error('Screening Error', err?.message || 'Failed to complete pre-donation screening.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6 max-w-3xl mx-auto">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Stethoscope className="h-6 w-6 text-rose-600" /> Pre-Donation Donor Screening
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Validate physiological parameters against clinical safety criteria before donation.
          </p>
        </div>

        <Button variant="outline" size="sm" onClick={() => navigate('/donations')}>
          <ArrowLeft className="h-4 w-4 mr-1" /> Back to Log
        </Button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Screening Form Card */}
        <Card className="border-slate-200 dark:border-slate-800">
          <CardHeader>
            <CardTitle className="text-lg font-semibold flex items-center gap-2">
              <HeartPulse className="h-5 w-5 text-rose-600" /> Clinical Vitals Form
            </CardTitle>
            <CardDescription>Enter donor physiological measurements</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleScreening} className="space-y-4">
              <div className="space-y-1.5">
                <Label htmlFor="donorProfileId">Donor Profile ID</Label>
                <Input
                  id="donorProfileId"
                  type="number"
                  required
                  value={formData.donorProfileId}
                  onChange={(e) => setFormData({ ...formData, donorProfileId: Number(e.target.value) })}
                />
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="weightKg">Weight (kg)</Label>
                  <Input
                    id="weightKg"
                    type="number"
                    step="0.1"
                    required
                    value={formData.weightKg}
                    onChange={(e) => setFormData({ ...formData, weightKg: Number(e.target.value) })}
                  />
                  <span className="text-[10px] text-slate-400">Min. 50.0 kg required</span>
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="hemoglobinGdl">Hemoglobin (g/dL)</Label>
                  <Input
                    id="hemoglobinGdl"
                    type="number"
                    step="0.1"
                    required
                    value={formData.hemoglobinGdl}
                    onChange={(e) => setFormData({ ...formData, hemoglobinGdl: Number(e.target.value) })}
                  />
                  <span className="text-[10px] text-slate-400">Min. 12.5 g/dL required</span>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="systolicBp">Systolic BP (mmHg)</Label>
                  <Input
                    id="systolicBp"
                    type="number"
                    required
                    value={formData.systolicBp}
                    onChange={(e) => setFormData({ ...formData, systolicBp: Number(e.target.value) })}
                  />
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="diastolicBp">Diastolic BP (mmHg)</Label>
                  <Input
                    id="diastolicBp"
                    type="number"
                    required
                    value={formData.diastolicBp}
                    onChange={(e) => setFormData({ ...formData, diastolicBp: Number(e.target.value) })}
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-1.5">
                  <Label htmlFor="pulseBpm">Pulse Rate (BPM)</Label>
                  <Input
                    id="pulseBpm"
                    type="number"
                    required
                    value={formData.pulseBpm}
                    onChange={(e) => setFormData({ ...formData, pulseBpm: Number(e.target.value) })}
                  />
                </div>

                <div className="space-y-1.5">
                  <Label htmlFor="temperatureCelsius">Temp (°C)</Label>
                  <Input
                    id="temperatureCelsius"
                    type="number"
                    step="0.1"
                    required
                    value={formData.temperatureCelsius}
                    onChange={(e) => setFormData({ ...formData, temperatureCelsius: Number(e.target.value) })}
                  />
                </div>
              </div>

              <Button
                type="submit"
                disabled={loading}
                className="w-full bg-rose-600 hover:bg-rose-700 text-white mt-2"
              >
                {loading ? 'Evaluating Vitals...' : 'Conduct Eligibility Screening'}
              </Button>
            </form>
          </CardContent>
        </Card>

        {/* Screening Results Card */}
        <Card className="border-slate-200 dark:border-slate-800 flex flex-col justify-between">
          <CardHeader>
            <CardTitle className="text-lg font-semibold">Screening Assessment</CardTitle>
            <CardDescription>Evaluation result against safety guidelines</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 flex flex-col items-center justify-center p-6 text-center">
            {result ? (
              <div className="space-y-4 w-full">
                <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-slate-100 dark:bg-slate-800">
                  {result.eligible ? (
                    <CheckCircle2 className="h-10 w-10 text-emerald-600" />
                  ) : (
                    <XCircle className="h-10 w-10 text-red-600" />
                  )}
                </div>

                <div>
                  <Badge variant={result.eligible ? 'success' : 'destructive'} className="text-sm px-3 py-1">
                    {result.eligible ? 'PASSED — ELIGIBLE TO DONATE' : 'DEFERRED — INELIGIBLE'}
                  </Badge>
                </div>

                {!result.eligible && result.rejectionReasons && (
                  <div className="rounded-lg bg-red-50 dark:bg-red-950/40 p-3 text-left border border-red-200 dark:border-red-900 text-xs text-red-700 dark:text-red-300 space-y-1">
                    <p className="font-bold">Deferral Reasons:</p>
                    <ul className="list-disc list-inside space-y-1">
                      {result.rejectionReasons.map((reason, idx) => (
                        <li key={idx}>{reason}</li>
                      ))}
                    </ul>
                  </div>
                )}

                {result.eligible && (
                  <p className="text-xs text-slate-600 dark:text-slate-400">
                    Donor meets all clinical safety guidelines. Proceed to record donation.
                  </p>
                )}
              </div>
            ) : (
              <div className="text-slate-400 text-sm py-10 space-y-2">
                <Stethoscope className="h-12 w-12 mx-auto text-slate-300" />
                <p>Fill in donor vitals and submit to evaluate eligibility.</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default EligibilityPage;
