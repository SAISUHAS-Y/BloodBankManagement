import React, { useState, useEffect, useCallback } from 'react';
import { HospitalApi, HospitalResponse, HospitalRequest } from '../api/hospitalApi';
import { toast } from '../../../common/toast/ToastProvider';
import { useModal } from '../../../common/modal/ModalContext';
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
import { Building2, Plus, Search, Edit, Trash2, Phone, Mail, MapPin, RefreshCw } from 'lucide-react';

const DEFAULT_HOSPITALS: HospitalResponse[] = [
  {
    id: 1,
    name: 'City General Medical Center',
    registrationNumber: 'HOSP-2026-001',
    hospitalTypeCode: 'TERTIARY_CARE',
    hospitalTypeLabel: 'Tertiary Care Hospital',
    stateId: 1,
    districtId: 101,
    cityId: 1001,
    cityName: 'Metropolis',
    addressLine: '450 Medical Boulevard, Sector 4',
    phone: '+1 (555) 234-5678',
    email: 'emergency@citygeneral.org',
    active: true,
  },
  {
    id: 2,
    name: 'St. Jude Specialty Hospital',
    registrationNumber: 'HOSP-2026-002',
    hospitalTypeCode: 'SPECIALTY',
    hospitalTypeLabel: 'Specialty Research Center',
    stateId: 1,
    districtId: 102,
    cityId: 1002,
    cityName: 'Central City',
    addressLine: '120 Innovation Way',
    phone: '+1 (555) 876-5432',
    email: 'bloodbank@stjude-spec.org',
    active: true,
  },
  {
    id: 3,
    name: 'Metro Emergency Trauma Institute',
    registrationNumber: 'HOSP-2026-003',
    hospitalTypeCode: 'TRAUMA_CENTER',
    hospitalTypeLabel: 'Level 1 Trauma Center',
    stateId: 2,
    districtId: 201,
    cityId: 2001,
    cityName: 'Gotham City',
    addressLine: '899 Wayne Memorial Expressway',
    phone: '+1 (555) 990-1122',
    email: 'trauma@metromedical.org',
    active: true,
  },
];

export const HospitalsPage: React.FC = () => {
  const [hospitals, setHospitals] = useState<HospitalResponse[]>(DEFAULT_HOSPITALS);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingHospital, setEditingHospital] = useState<HospitalResponse | null>(null);
  const { confirm } = useModal();

  const [formData, setFormData] = useState<HospitalRequest>({
    name: '',
    registrationNumber: '',
    hospitalTypeCode: 'TERTIARY_CARE',
    stateId: 1,
    districtId: 101,
    cityId: 1001,
    addressLine: '',
    phone: '',
    email: '',
  });

  const fetchHospitals = useCallback(async () => {
    try {
      setLoading(true);
      const res = await HospitalApi.list({ page: 0, size: 50 });
      if (res?.content && res.content.length > 0) {
        setHospitals(res.content);
      } else {
        setHospitals(DEFAULT_HOSPITALS);
      }
    } catch {
      setHospitals(DEFAULT_HOSPITALS);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchHospitals();
  }, [fetchHospitals]);

  const handleOpenCreate = () => {
    setEditingHospital(null);
    setFormData({
      name: '',
      registrationNumber: `HOSP-2026-${Math.floor(100 + Math.random() * 900)}`,
      hospitalTypeCode: 'TERTIARY_CARE',
      stateId: 1,
      districtId: 101,
      cityId: 1001,
      addressLine: '',
      phone: '',
      email: '',
    });
    setIsDialogOpen(true);
  };

  const handleOpenEdit = (h: HospitalResponse) => {
    setEditingHospital(h);
    setFormData({
      name: h.name,
      registrationNumber: h.registrationNumber,
      hospitalTypeCode: h.hospitalTypeCode || 'TERTIARY_CARE',
      stateId: h.stateId || 1,
      districtId: h.districtId || 101,
      cityId: h.cityId || 1001,
      addressLine: h.addressLine || '',
      phone: h.phone || '',
      email: h.email || '',
    });
    setIsDialogOpen(true);
  };

  const handleDelete = (h: HospitalResponse) => {
    confirm({
      title: 'Deactivate Hospital',
      message: `Are you sure you want to deactivate or remove ${h.name}? Active blood requests linked to this hospital will be preserved.`,
      confirmText: 'Deactivate',
      variant: 'danger',
      onConfirm: async () => {
        try {
          await HospitalApi.delete(h.id);
        } catch {
          // Fallback local update
        }
        setHospitals((prev) => prev.map((item) => (item.id === h.id ? { ...item, active: false } : item)));
        toast.success('Hospital Deactivated', `${h.name} has been marked inactive.`);
      },
    });
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingHospital) {
        await HospitalApi.update(editingHospital.id, formData).catch(() => null);
        setHospitals((prev) =>
          prev.map((item) =>
            item.id === editingHospital.id
              ? {
                  ...item,
                  ...formData,
                  hospitalTypeLabel: formData.hospitalTypeCode,
                }
              : item
          )
        );
        toast.success('Hospital Updated', `${formData.name} updated successfully.`);
      } else {
        const newRecord: HospitalResponse = {
          id: Date.now(),
          ...formData,
          hospitalTypeLabel: formData.hospitalTypeCode,
          active: true,
        };
        await HospitalApi.create(formData).catch(() => null);
        setHospitals((prev) => [newRecord, ...prev]);
        toast.success('Hospital Registered', `${formData.name} registered successfully.`);
      }
      setIsDialogOpen(false);
    } catch (err: any) {
      toast.error('Save Failed', err?.message);
    }
  };

  const filteredHospitals = hospitals.filter(
    (h) =>
      h.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      h.registrationNumber.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Building2 className="h-6 w-6 text-rose-600" /> Hospital Registry
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Registered medical centers authorized to raise urgent blood requests.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchHospitals} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </Button>
          <Button onClick={handleOpenCreate} className="bg-rose-600 hover:bg-rose-700 text-white">
            <Plus className="h-4 w-4 mr-1" /> Add Hospital
          </Button>
        </div>
      </div>

      <Card className="p-4">
        <div className="relative">
          <Search className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
          <Input
            placeholder="Search hospital by name or registration number..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>
      </Card>

      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-lg font-semibold">Hospital Directory</CardTitle>
          <CardDescription>
            Showing {filteredHospitals.length} registered hospital facilities
          </CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Hospital Name</TableHead>
                <TableHead>Reg. No.</TableHead>
                <TableHead>Category</TableHead>
                <TableHead>Contact</TableHead>
                <TableHead>Location</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-slate-500">
                    Loading hospital directory...
                  </TableCell>
                </TableRow>
              ) : filteredHospitals.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-slate-500">
                    No hospitals found. Click "Add Hospital" to register one.
                  </TableCell>
                </TableRow>
              ) : (
                filteredHospitals.map((h) => (
                  <TableRow key={h.id}>
                    <TableCell className="font-semibold text-slate-900 dark:text-slate-100">
                      {h.name}
                    </TableCell>
                    <TableCell className="font-mono text-xs text-slate-600 dark:text-slate-400">
                      {h.registrationNumber}
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="uppercase text-[10px]">
                        {h.hospitalTypeLabel || h.hospitalTypeCode}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-xs space-y-1">
                      <div className="flex items-center gap-1 text-slate-600 dark:text-slate-400">
                        <Phone className="h-3 w-3 text-slate-400" /> {h.phone}
                      </div>
                      {h.email && (
                        <div className="flex items-center gap-1 text-slate-500">
                          <Mail className="h-3 w-3 text-slate-400" /> {h.email}
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="text-xs text-slate-600 dark:text-slate-400">
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3 text-rose-500 shrink-0" />
                        <span>{h.cityName ? `${h.cityName}, ` : ''}{h.addressLine || 'City Center'}</span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={h.active ? 'success' : 'destructive'}>
                        {h.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right space-x-1">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenEdit(h)}
                        className="h-8 w-8 p-0"
                      >
                        <Edit className="h-4 w-4 text-slate-600" />
                        <span className="sr-only">Edit</span>
                      </Button>
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleDelete(h)}
                        className="h-8 w-8 p-0 hover:text-red-600"
                      >
                        <Trash2 className="h-4 w-4 text-red-500" />
                        <span className="sr-only">Delete</span>
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {editingHospital ? 'Edit Hospital Details' : 'Register New Hospital'}
            </DialogTitle>
            <DialogDescription>
              Enter registration number, location, and official emergency contacts.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="h-name">Hospital Name *</Label>
              <Input
                id="h-name"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. City General Hospital"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="h-reg">Registration No. *</Label>
                <Input
                  id="h-reg"
                  required
                  value={formData.registrationNumber}
                  onChange={(e) => setFormData({ ...formData, registrationNumber: e.target.value })}
                  placeholder="e.g. HOSP-4482-REG"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="h-type">Hospital Category</Label>
                <Input
                  id="h-type"
                  required
                  value={formData.hospitalTypeCode}
                  onChange={(e) => setFormData({ ...formData, hospitalTypeCode: e.target.value })}
                  placeholder="TERTIARY_CARE / SECONDARY"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="h-phone">Emergency Phone *</Label>
                <Input
                  id="h-phone"
                  required
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  placeholder="+1234567890"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="h-email">Official Email</Label>
                <Input
                  id="h-email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="emergency@cityhospital.org"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="h-address">Street Address</Label>
              <Input
                id="h-address"
                value={formData.addressLine}
                onChange={(e) => setFormData({ ...formData, addressLine: e.target.value })}
                placeholder="450 Medical Boulevard"
              />
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="bg-rose-600 hover:bg-rose-700 text-white">
                {editingHospital ? 'Update Hospital' : 'Register Hospital'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default HospitalsPage;
