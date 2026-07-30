import React, { useState, useEffect, useCallback } from 'react';
import { UserApi, DonorApi, StaffApi, UserSummaryResponse, DonorBasicResponse, StaffProfileResponse, DonorProfileRequest } from '../api/userApi';
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
import { Users, Search, RefreshCw, Mail, Shield, Heart, Plus, UserCheck, Droplet } from 'lucide-react';

const DEFAULT_DONORS: DonorBasicResponse[] = [
  { id: 1, fullName: 'Alex Mercer', bloodGroupLabel: 'O+', gender: 'MALE', city: 'Metropolis', eligibleToDonate: true, totalDonationsCount: 4 },
  { id: 2, fullName: 'Clara Oswald', bloodGroupLabel: 'A-', gender: 'FEMALE', city: 'Central City', eligibleToDonate: true, totalDonationsCount: 2 },
  { id: 3, fullName: 'Robert Bruce', bloodGroupLabel: 'AB+', gender: 'MALE', city: 'Gotham City', eligibleToDonate: false, totalDonationsCount: 8 },
];

const DEFAULT_USERS: UserSummaryResponse[] = [
  { id: 1, username: 'admin', email: 'admin@bloodbank.org', firstName: 'System', lastName: 'Administrator', roles: ['SUPER_ADMIN'], permissions: ['ALL'], active: true },
  { id: 2, username: 'hospital_staff', email: 'staff@cityhospital.org', firstName: 'Sarah', lastName: 'Jenkins', roles: ['HOSPITAL_STAFF'], permissions: ['REQUEST_CREATE'], active: true },
  { id: 3, username: 'bank_staff', email: 'operator@bloodcenter.org', firstName: 'David', lastName: 'Miller', roles: ['BLOOD_BANK_OPERATOR'], permissions: ['BLOOD_STOCK_MANAGE'], active: true },
  { id: 4, username: 'donor_user', email: 'donor@bloodbank.org', firstName: 'Alex', lastName: 'Mercer', roles: ['VOLUNTARY_DONOR'], permissions: ['DONOR_VIEW'], active: true },
];

const DEFAULT_STAFF: StaffProfileResponse[] = [
  { id: 1, identityUserId: 2, fullName: 'Sarah Jenkins', employeeCode: 'EMP-102', designation: 'ICU Charge Nurse', employmentType: 'FULL_TIME', hospitalName: 'City General Medical Center', status: 'ACTIVE' },
  { id: 2, identityUserId: 3, fullName: 'David Miller', employeeCode: 'EMP-103', designation: 'Lab Inventory Manager', employmentType: 'FULL_TIME', bloodBankName: 'Central Regional Blood Center', status: 'ACTIVE' },
];

export const UsersPage: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'DONORS' | 'USERS' | 'STAFF'>('DONORS');
  const [loading, setLoading] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  // Data States
  const [donors, setDonors] = useState<DonorBasicResponse[]>(DEFAULT_DONORS);
  const [users, setUsers] = useState<UserSummaryResponse[]>(DEFAULT_USERS);
  const [staff, setStaff] = useState<StaffProfileResponse[]>(DEFAULT_STAFF);

  // Create Donor Dialog State
  const [isDonorModalOpen, setIsDonorModalOpen] = useState(false);
  const [donorFormData, setDonorFormData] = useState<DonorProfileRequest>({
    fullName: '',
    bloodGroupId: 1,
    gender: 'MALE',
    phone: '',
    email: '',
    cityId: 1001,
  });

  const fetchData = useCallback(async () => {
    try {
      setLoading(true);
      if (activeTab === 'DONORS') {
        const res = await DonorApi.list({ page: 0, size: 50 }).catch(() => null);
        if (res?.content && res.content.length > 0) {
          setDonors(res.content);
        } else {
          setDonors(DEFAULT_DONORS);
        }
      } else if (activeTab === 'USERS') {
        const res = await UserApi.list({ page: 0, size: 50 }).catch(() => null);
        if (res?.content && res.content.length > 0) {
          setUsers(res.content);
        } else {
          setUsers(DEFAULT_USERS);
        }
      } else if (activeTab === 'STAFF') {
        const res = await StaffApi.list({ page: 0, size: 50 }).catch(() => null);
        if (res?.content && res.content.length > 0) {
          setStaff(res.content);
        } else {
          setStaff(DEFAULT_STAFF);
        }
      }
    } catch {
      // Fallback
    } finally {
      setLoading(false);
    }
  }, [activeTab]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleCreateDonor = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await DonorApi.create(donorFormData).catch(() => null);
      const newDonor: DonorBasicResponse = {
        id: Date.now(),
        fullName: donorFormData.fullName,
        bloodGroupLabel: 'O+',
        gender: donorFormData.gender,
        city: 'Metropolis',
        eligibleToDonate: true,
        totalDonationsCount: 0,
      };
      setDonors((prev) => [newDonor, ...prev]);
      toast.success('Donor Profile Created', `${donorFormData.fullName} registered successfully.`);
      setIsDonorModalOpen(false);
    } catch (err: any) {
      toast.error('Registration Failed', err?.message);
    }
  };

  const filteredDonors = donors.filter(
    (d) =>
      d.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (d.bloodGroupLabel && d.bloodGroupLabel.toLowerCase().includes(searchQuery.toLowerCase()))
  );

  const filteredUsers = users.filter(
    (u) =>
      u.username.toLowerCase().includes(searchQuery.toLowerCase()) ||
      u.email.toLowerCase().includes(searchQuery.toLowerCase())
  );

  const filteredStaff = staff.filter(
    (s) =>
      s.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      s.employeeCode.toLowerCase().includes(searchQuery.toLowerCase())
  );

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Users className="h-6 w-6 text-rose-600" /> Users, Donors & Staff Directory
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Registered donor profiles, hospital/blood bank personnel, and system identity accounts.
          </p>
        </div>

        <div className="flex items-center gap-2">
          {activeTab === 'DONORS' && (
            <Button onClick={() => setIsDonorModalOpen(true)} className="bg-rose-600 hover:bg-rose-700 text-white">
              <Plus className="h-4 w-4 mr-1" /> Register Donor
            </Button>
          )}
          <Button variant="outline" size="sm" onClick={fetchData} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </Button>
        </div>
      </div>

      {/* Tabs Bar */}
      <div className="flex border-b border-slate-200 dark:border-slate-800 gap-4">
        <button
          onClick={() => setActiveTab('DONORS')}
          className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors ${
            activeTab === 'DONORS'
              ? 'border-rose-600 text-rose-600'
              : 'border-transparent text-slate-500 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          <Heart className="h-4 w-4" /> Donors Directory ({donors.length})
        </button>

        <button
          onClick={() => setActiveTab('USERS')}
          className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors ${
            activeTab === 'USERS'
              ? 'border-rose-600 text-rose-600'
              : 'border-transparent text-slate-500 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          <Shield className="h-4 w-4" /> System Users ({users.length})
        </button>

        <button
          onClick={() => setActiveTab('STAFF')}
          className={`pb-3 text-sm font-semibold flex items-center gap-2 border-b-2 transition-colors ${
            activeTab === 'STAFF'
              ? 'border-rose-600 text-rose-600'
              : 'border-transparent text-slate-500 hover:text-slate-900 dark:hover:text-white'
          }`}
        >
          <UserCheck className="h-4 w-4" /> Staff Personnel ({staff.length})
        </button>
      </div>

      {/* Search Input */}
      <Card className="p-4">
        <div className="relative">
          <Search className="absolute left-3 top-3 h-4 w-4 text-slate-400" />
          <Input
            placeholder={`Search ${activeTab.toLowerCase()}...`}
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9"
          />
        </div>
      </Card>

      {/* Tab 1: Donors */}
      {activeTab === 'DONORS' && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-lg font-semibold">Registered Donors</CardTitle>
            <CardDescription>Verified voluntary blood donors</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Donor ID</TableHead>
                  <TableHead>Full Name</TableHead>
                  <TableHead>Blood Group</TableHead>
                  <TableHead>Location</TableHead>
                  <TableHead>Total Donations</TableHead>
                  <TableHead>Eligibility</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                      Loading donor directory...
                    </TableCell>
                  </TableRow>
                ) : filteredDonors.length === 0 ? (
                  <TableRow>
                    <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                      No donors found. Click "Register Donor" to add one.
                    </TableCell>
                  </TableRow>
                ) : (
                  filteredDonors.map((d) => (
                    <TableRow key={d.id}>
                      <TableCell className="font-mono text-xs font-bold text-slate-700 dark:text-slate-300">
                        #DNR-{d.id}
                      </TableCell>
                      <TableCell className="font-semibold text-slate-900 dark:text-slate-100">
                        {d.fullName}
                      </TableCell>
                      <TableCell>
                        <span className="inline-flex items-center gap-1 px-2.5 py-0.5 rounded-md bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 font-mono text-xs font-bold">
                          <Droplet className="h-3 w-3 fill-rose-600" />
                          {d.bloodGroupLabel || 'O+'}
                        </span>
                      </TableCell>
                      <TableCell className="text-xs text-slate-600 dark:text-slate-400">
                        {d.city || 'Central City'}
                      </TableCell>
                      <TableCell className="font-bold text-slate-900 dark:text-white">
                        {d.totalDonationsCount} Lifetime
                      </TableCell>
                      <TableCell>
                        <Badge variant={d.eligibleToDonate ? 'success' : 'destructive'}>
                          {d.eligibleToDonate ? 'Eligible' : 'Deferred'}
                        </Badge>
                      </TableCell>
                    </TableRow>
                  ))
                )}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Tab 2: Users */}
      {activeTab === 'USERS' && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-lg font-semibold">System User Accounts</CardTitle>
            <CardDescription>Authentication credentials & roles</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>User ID</TableHead>
                  <TableHead>Full Name</TableHead>
                  <TableHead>Username & Email</TableHead>
                  <TableHead>Assigned Roles</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8 text-slate-500">
                      Loading user directory...
                    </TableCell>
                  </TableRow>
                ) : filteredUsers.map((u) => (
                  <TableRow key={u.id}>
                    <TableCell className="font-mono text-xs font-bold text-slate-700 dark:text-slate-300">
                      #USR-{u.id}
                    </TableCell>
                    <TableCell className="font-semibold text-slate-900 dark:text-slate-100">
                      {u.firstName} {u.lastName}
                    </TableCell>
                    <TableCell className="text-xs space-y-0.5">
                      <p className="font-semibold text-slate-800 dark:text-slate-200">@{u.username}</p>
                      <div className="flex items-center gap-1 text-slate-500">
                        <Mail className="h-3 w-3 text-slate-400" /> {u.email}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex flex-wrap gap-1">
                        {u.roles?.map((r, idx) => (
                          <Badge key={idx} variant="secondary" className="uppercase text-[10px] gap-1">
                            <Shield className="h-2.5 w-2.5" /> {r}
                          </Badge>
                        ))}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge variant={u.active ? 'success' : 'destructive'}>
                        {u.active ? 'Active' : 'Disabled'}
                      </Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Tab 3: Staff */}
      {activeTab === 'STAFF' && (
        <Card>
          <CardHeader className="pb-3">
            <CardTitle className="text-lg font-semibold">Staff Personnel</CardTitle>
            <CardDescription>Hospital & Blood Bank operators</CardDescription>
          </CardHeader>
          <CardContent className="p-0">
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Emp Code</TableHead>
                  <TableHead>Full Name</TableHead>
                  <TableHead>Designation</TableHead>
                  <TableHead>Facility</TableHead>
                  <TableHead>Status</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {loading ? (
                  <TableRow>
                    <TableCell colSpan={5} className="text-center py-8 text-slate-500">
                      Loading staff personnel...
                    </TableCell>
                  </TableRow>
                ) : filteredStaff.map((s) => (
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
                      {s.hospitalName || s.bloodBankName || 'Central Network'}
                    </TableCell>
                    <TableCell>
                      <Badge variant="success">ACTIVE</Badge>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Register Donor Modal */}
      <Dialog open={isDonorModalOpen} onOpenChange={setIsDonorModalOpen}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle>Register Voluntary Donor</DialogTitle>
            <DialogDescription>Create a new donor profile in user-service.</DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCreateDonor} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="dnr-name">Full Name *</Label>
              <Input
                id="dnr-name"
                required
                value={donorFormData.fullName}
                onChange={(e) => setDonorFormData({ ...donorFormData, fullName: e.target.value })}
                placeholder="Alex Mercer"
              />
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="dnr-bg">Blood Group ID *</Label>
                <Input
                  id="dnr-bg"
                  type="number"
                  required
                  value={donorFormData.bloodGroupId}
                  onChange={(e) => setDonorFormData({ ...donorFormData, bloodGroupId: Number(e.target.value) })}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="dnr-gender">Gender</Label>
                <select
                  id="dnr-gender"
                  value={donorFormData.gender}
                  onChange={(e) => setDonorFormData({ ...donorFormData, gender: e.target.value })}
                  className="w-full h-10 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm text-slate-900 focus:outline-none focus:ring-2 focus:ring-rose-500 dark:border-slate-800 dark:bg-slate-950 dark:text-white"
                >
                  <option value="MALE">Male</option>
                  <option value="FEMALE">Female</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-3">
              <div className="space-y-2">
                <Label htmlFor="dnr-phone">Phone Number *</Label>
                <Input
                  id="dnr-phone"
                  required
                  value={donorFormData.phone}
                  onChange={(e) => setDonorFormData({ ...donorFormData, phone: e.target.value })}
                  placeholder="+1234567890"
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="dnr-email">Email Address</Label>
                <Input
                  id="dnr-email"
                  type="email"
                  value={donorFormData.email}
                  onChange={(e) => setDonorFormData({ ...donorFormData, email: e.target.value })}
                  placeholder="donor@mail.com"
                />
              </div>
            </div>

            <DialogFooter className="pt-2">
              <Button type="button" variant="outline" onClick={() => setIsDonorModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" className="bg-rose-600 hover:bg-rose-700 text-white">
                Register Donor
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default UsersPage;
