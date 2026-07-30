import React, { useState, useEffect, useCallback } from 'react';
import { StaffApi } from '../api/staffApi';
import { StaffProfileResponse, StaffProfileRequest } from '../../users/api/userApi';
import { toast } from '../../../common/toast/ToastProvider';
import { Button } from '../../../shared/components/ui/button';
import { Card, CardHeader, CardTitle, CardDescription, CardContent } from '../../../shared/components/ui/card';
import { Table, TableHeader, TableBody, TableHead, TableRow, TableCell } from '../../../shared/components/ui/table';
import { Badge } from '../../../shared/components/ui/badge';
import { Input } from '../../../shared/components/ui/input';
import { Label } from '../../../shared/components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from '../../../shared/components/ui/dialog';
import { Users, Plus, RefreshCw, Building2, UserCheck } from 'lucide-react';
import { useNavigate } from 'react-router';

export const HospitalStaffPage: React.FC = () => {
  const [staffList, setStaffList] = useState<StaffProfileResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const navigate = useNavigate();

  const [formData, setFormData] = useState<StaffProfileRequest>({
    identityUserId: 1,
    fullName: '',
    employeeCode: '',
    designation: 'Staff Nurse',
    employmentType: 'FULL_TIME',
    hospitalId: 1,
  });

  const fetchStaff = useCallback(async () => {
    try {
      setLoading(true);
      const res = await StaffApi.list({ page: 0, size: 50 });
      setStaffList(res?.content || []);
    } catch (err: any) {
      toast.error('Failed to load staff list', err?.message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchStaff();
  }, [fetchStaff]);

  const handleCreateStaff = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await StaffApi.create(formData);
      toast.success('Staff Member Added', `${formData.fullName} registered as hospital staff.`);
      setIsDialogOpen(false);
      fetchStaff();
    } catch (err: any) {
      toast.error('Registration Failed', err?.message);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <UserCheck className="h-6 w-6 text-rose-600" /> Hospital Staff Management
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Manage authorized hospital personnel and emergency blood request operators.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={() => navigate('/hospitals')}>
            <Building2 className="h-4 w-4 mr-1" /> Hospital Directory
          </Button>
          <Button variant="outline" size="sm" onClick={fetchStaff} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </Button>
          <Button onClick={() => setIsDialogOpen(true)} className="bg-rose-600 hover:bg-rose-700 text-white">
            <Plus className="h-4 w-4 mr-1" /> Register Staff
          </Button>
        </div>
      </div>

      <Card>
        <CardHeader>
          <CardTitle className="text-lg font-semibold">Registered Staff Directory</CardTitle>
          <CardDescription>Hospital personnel authorized to raise emergency requests</CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Emp Code</TableHead>
                <TableHead>Full Name</TableHead>
                <TableHead>Designation</TableHead>
                <TableHead>Affiliated Hospital</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                    Loading staff directory...
                  </TableCell>
                </TableRow>
              ) : staffList.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                    No hospital staff members found. Click "Register Staff" to add one.
                  </TableCell>
                </TableRow>
              ) : (
                staffList.map((s) => (
                  <TableRow key={s.id}>
                    <TableCell className="font-mono text-xs font-bold text-slate-700 dark:text-slate-300">
                      {s.employeeCode || `#EMP-${s.id}`}
                    </TableCell>
                    <TableCell className="font-semibold text-slate-900 dark:text-slate-100">
                      {s.fullName}
                    </TableCell>
                    <TableCell className="text-xs text-slate-600 dark:text-slate-400">
                      {s.designation}
                    </TableCell>
                    <TableCell className="text-xs font-medium text-slate-800 dark:text-slate-200">
                      {s.hospitalName || `Hospital #${s.hospitalId || 'N/A'}`}
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="uppercase text-[10px]">
                        {s.employmentType || 'FULL_TIME'}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      <Badge variant="success">ACTIVE</Badge>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Register Staff Modal */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Register Hospital Staff Member</DialogTitle>
            <DialogDescription>
              Associate an identity user with a hospital facility for request permissions.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCreateStaff} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="staff-user">Identity User ID *</Label>
              <Input
                id="staff-user"
                type="number"
                required
                value={formData.identityUserId}
                onChange={(e) => setFormData({ ...formData, identityUserId: Number(e.target.value) })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="staff-name">Full Name *</Label>
              <Input
                id="staff-name"
                required
                value={formData.fullName}
                onChange={(e) => setFormData({ ...formData, fullName: e.target.value })}
                placeholder="Dr. Emily Watson"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="staff-emp">Employee Code *</Label>
                <Input
                  id="staff-emp"
                  required
                  value={formData.employeeCode}
                  onChange={(e) => setFormData({ ...formData, employeeCode: e.target.value })}
                  placeholder="EMP-8842"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="staff-desig">Designation</Label>
                <Input
                  id="staff-desig"
                  value={formData.designation}
                  onChange={(e) => setFormData({ ...formData, designation: e.target.value })}
                  placeholder="ICU Nurse / Doctor"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="staff-hosp">Hospital ID *</Label>
              <Input
                id="staff-hosp"
                type="number"
                required
                value={formData.hospitalId}
                onChange={(e) => setFormData({ ...formData, hospitalId: Number(e.target.value) })}
              />
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="bg-rose-600 hover:bg-rose-700 text-white">
                Register Staff
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default HospitalStaffPage;
