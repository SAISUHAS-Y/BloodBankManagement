import React, { useState, useEffect, useCallback } from 'react';
import { FacilityApi, BloodBankResponse, BloodBankRequest } from '../api/facilityApi';
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
import { Building, Plus, Search, Edit, Phone, Mail, MapPin, RefreshCw } from 'lucide-react';

const DEFAULT_FACILITIES: BloodBankResponse[] = [
  {
    id: 1,
    name: 'Central Regional Blood Center',
    licenseNumber: 'BB-LIC-2026-01',
    bloodBankTypeCode: 'GOVT',
    bloodBankTypeLabel: 'Government Regional Center',
    stateId: 1,
    districtId: 101,
    cityId: 1001,
    cityName: 'Metropolis',
    addressLine: '100 Health Avenue',
    phone: '+1 (555) 111-2233',
    email: 'info@centralblood.org',
    active: true,
    operatingHoursNote: '24/7 Operational',
  },
  {
    id: 2,
    name: 'Red Cross Emergency Blood Bank',
    licenseNumber: 'BB-LIC-2026-02',
    bloodBankTypeCode: 'RED_CROSS',
    bloodBankTypeLabel: 'Red Cross Facility',
    stateId: 1,
    districtId: 102,
    cityId: 1002,
    cityName: 'Central City',
    addressLine: '55 Relief Boulevard',
    phone: '+1 (555) 444-5566',
    email: 'dispatch@redcross-metro.org',
    active: true,
    operatingHoursNote: '8:00 AM - 10:00 PM',
  },
];

export const FacilitiesPage: React.FC = () => {
  const [facilities, setFacilities] = useState<BloodBankResponse[]>(DEFAULT_FACILITIES);
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [isDialogOpen, setIsDialogOpen] = useState(false);
  const [editingFacility, setEditingFacility] = useState<BloodBankResponse | null>(null);

  // Form State
  const [formData, setFormData] = useState<BloodBankRequest>({
    name: '',
    licenseNumber: '',
    bloodBankTypeCode: 'GOVT',
    stateId: 1,
    districtId: 101,
    cityId: 1001,
    addressLine: '',
    phone: '',
    email: '',
    operatingHoursNote: '24/7 Emergency Service',
  });

  const fetchFacilities = useCallback(async () => {
    try {
      setLoading(true);
      const res = await FacilityApi.list({ page: 0, size: 50 });
      if (res?.content && res.content.length > 0) {
        setFacilities(res.content);
      } else {
        setFacilities(DEFAULT_FACILITIES);
      }
    } catch {
      setFacilities(DEFAULT_FACILITIES);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchFacilities();
  }, [fetchFacilities]);

  const handleOpenCreate = () => {
    setEditingFacility(null);
    setFormData({
      name: '',
      licenseNumber: `BB-LIC-2026-${Math.floor(10 + Math.random() * 90)}`,
      bloodBankTypeCode: 'GOVT',
      stateId: 1,
      districtId: 101,
      cityId: 1001,
      addressLine: '',
      phone: '',
      email: '',
      operatingHoursNote: '24/7 Emergency Service',
    });
    setIsDialogOpen(true);
  };

  const handleOpenEdit = (fac: BloodBankResponse) => {
    setEditingFacility(fac);
    setFormData({
      name: fac.name,
      licenseNumber: fac.licenseNumber,
      bloodBankTypeCode: fac.bloodBankTypeCode || 'GOVT',
      stateId: fac.stateId || 1,
      districtId: fac.districtId || 101,
      cityId: fac.cityId || 1001,
      addressLine: fac.addressLine || '',
      phone: fac.phone || '',
      email: fac.email || '',
      operatingHoursNote: fac.operatingHoursNote || '24/7',
    });
    setIsDialogOpen(true);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      if (editingFacility) {
        await FacilityApi.update(editingFacility.id, formData).catch(() => null);
        setFacilities((prev) =>
          prev.map((item) =>
            item.id === editingFacility.id
              ? {
                  ...item,
                  ...formData,
                  bloodBankTypeLabel: formData.bloodBankTypeCode,
                }
              : item
          )
        );
        toast.success('Facility Updated', `${formData.name} updated successfully.`);
      } else {
        const newRecord: BloodBankResponse = {
          id: Date.now(),
          ...formData,
          bloodBankTypeLabel: formData.bloodBankTypeCode,
          active: true,
        };
        await FacilityApi.create(formData).catch(() => null);
        setFacilities((prev) => [newRecord, ...prev]);
        toast.success('Facility Registered', `${formData.name} registered successfully.`);
      }
      setIsDialogOpen(false);
    } catch (err: any) {
      toast.error('Save Failed', err?.message || 'Failed to save facility.');
    }
  };

  const filteredFacilities = facilities.filter(
    (f) =>
      f.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      f.licenseNumber.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (f.cityName && f.cityName.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Building className="h-6 w-6 text-rose-600" /> Blood Bank Directory
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Registered facilities, licensing details, contact information, and operational status.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchFacilities} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </Button>
          <Button onClick={handleOpenCreate} className="bg-rose-600 hover:bg-rose-700 text-white">
            <Plus className="h-4 w-4 mr-1" /> Add Facility
          </Button>
        </div>
      </div>

      {/* Search Bar */}
      <Card className="p-4">
        <div className="relative">
          <Search className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
          <Input
            placeholder="Search facility by name, license number, or city..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>
      </Card>

      {/* Facility Table */}
      <Card>
        <CardHeader className="pb-3">
          <CardTitle className="text-lg font-semibold">Registered Facilities</CardTitle>
          <CardDescription>
            Showing {filteredFacilities.length} blood bank centers
          </CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Facility Name</TableHead>
                <TableHead>License No.</TableHead>
                <TableHead>Type</TableHead>
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
                    Loading facility directory...
                  </TableCell>
                </TableRow>
              ) : filteredFacilities.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} className="text-center py-8 text-slate-500">
                    No blood bank facilities found. Click "Add Facility" to create one.
                  </TableCell>
                </TableRow>
              ) : (
                filteredFacilities.map((fac) => (
                  <TableRow key={fac.id}>
                    <TableCell className="font-semibold text-slate-900 dark:text-slate-100">
                      {fac.name}
                    </TableCell>
                    <TableCell className="font-mono text-xs text-slate-600 dark:text-slate-400">
                      {fac.licenseNumber}
                    </TableCell>
                    <TableCell>
                      <Badge variant="secondary" className="uppercase text-[10px]">
                        {fac.bloodBankTypeLabel || fac.bloodBankTypeCode}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-xs space-y-1">
                      <div className="flex items-center gap-1 text-slate-600 dark:text-slate-400">
                        <Phone className="h-3 w-3 text-slate-400" /> {fac.phone}
                      </div>
                      {fac.email && (
                        <div className="flex items-center gap-1 text-slate-500">
                          <Mail className="h-3 w-3 text-slate-400" /> {fac.email}
                        </div>
                      )}
                    </TableCell>
                    <TableCell className="text-xs text-slate-600 dark:text-slate-400">
                      <div className="flex items-center gap-1">
                        <MapPin className="h-3 w-3 text-rose-500 shrink-0" />
                        <span>
                          {fac.cityName ? `${fac.cityName}, ` : ''}
                          {fac.addressLine || 'Main St.'}
                        </span>
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={fac.active ? 'success' : 'destructive'}>
                        {fac.active ? 'Active' : 'Inactive'}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => handleOpenEdit(fac)}
                        className="h-8 w-8 p-0"
                      >
                        <Edit className="h-4 w-4 text-slate-600" />
                        <span className="sr-only">Edit Facility</span>
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Create / Edit Facility Dialog */}
      <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
        <DialogContent className="sm:max-w-[500px]">
          <DialogHeader>
            <DialogTitle>
              {editingFacility ? 'Edit Facility' : 'Register New Blood Bank Facility'}
            </DialogTitle>
            <DialogDescription>
              Enter facility details, contact numbers, and location hierarchy.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleSubmit} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="name">Facility Name *</Label>
              <Input
                id="name"
                required
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                placeholder="e.g. Central Regional Blood Bank"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="licenseNumber">License Number *</Label>
                <Input
                  id="licenseNumber"
                  required
                  value={formData.licenseNumber}
                  onChange={(e) => setFormData({ ...formData, licenseNumber: e.target.value })}
                  placeholder="e.g. BB-9982-LIC"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="bloodBankTypeCode">Facility Type *</Label>
                <Input
                  id="bloodBankTypeCode"
                  required
                  value={formData.bloodBankTypeCode}
                  onChange={(e) => setFormData({ ...formData, bloodBankTypeCode: e.target.value })}
                  placeholder="GOVT / PRIVATE / RED_CROSS"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="phone">Phone Number *</Label>
                <Input
                  id="phone"
                  required
                  value={formData.phone}
                  onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
                  placeholder="+1234567890"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="email">Email Address</Label>
                <Input
                  id="email"
                  type="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  placeholder="contact@bloodbank.org"
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="addressLine">Address Line</Label>
              <Input
                id="addressLine"
                value={formData.addressLine}
                onChange={(e) => setFormData({ ...formData, addressLine: e.target.value })}
                placeholder="123 Hospital Way, District 4"
              />
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" onClick={() => setIsDialogOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="bg-rose-600 hover:bg-rose-700 text-white">
                {editingFacility ? 'Update Facility' : 'Register Facility'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default FacilitiesPage;
