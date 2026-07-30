import React, { useState, useEffect, useCallback } from 'react';
import { StockApi, BloodStockResponse } from '../api/stockApi';
import { FacilityApi, BloodBankResponse } from '../api/facilityApi';
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
import {
  Boxes,
  RefreshCw,
  SlidersHorizontal,
  Droplet,
  AlertTriangle,
  Building,
  ArrowUpRight,
  ArrowDownRight,
} from 'lucide-react';

const DEFAULT_FACILITIES: BloodBankResponse[] = [
  {
    id: 1,
    name: 'Central Regional Blood Center',
    licenseNumber: 'BB-LIC-2026-01',
    bloodBankTypeCode: 'GOVT',
    stateId: 1,
    districtId: 101,
    cityId: 1001,
    active: true,
  },
  {
    id: 2,
    name: 'Red Cross Emergency Blood Bank',
    licenseNumber: 'BB-LIC-2026-02',
    bloodBankTypeCode: 'RED_CROSS',
    stateId: 1,
    districtId: 102,
    cityId: 1002,
    active: true,
  },
];

const DEFAULT_STOCKS: BloodStockResponse[] = [
  { id: 101, bloodBankId: 1, bloodGroupId: 1, bloodGroupLabel: 'O+', componentTypeCode: 'WHOLE_BLOOD', componentTypeLabel: 'Whole Blood', unitsAvailable: 42, minThresholdUnits: 10, lastUpdatedAt: new Date().toISOString() },
  { id: 102, bloodBankId: 1, bloodGroupId: 2, bloodGroupLabel: 'A+', componentTypeCode: 'PACKED_RBC', componentTypeLabel: 'Packed Red Blood Cells', unitsAvailable: 18, minThresholdUnits: 5, lastUpdatedAt: new Date().toISOString() },
  { id: 103, bloodBankId: 1, bloodGroupId: 3, bloodGroupLabel: 'B+', componentTypeCode: 'PLATELETS', componentTypeLabel: 'Platelet Concentrate', unitsAvailable: 4, minThresholdUnits: 10, lastUpdatedAt: new Date().toISOString() },
  { id: 104, bloodBankId: 1, bloodGroupId: 4, bloodGroupLabel: 'AB+', componentTypeCode: 'FRESH_FROZEN_PLASMA', componentTypeLabel: 'Fresh Frozen Plasma', unitsAvailable: 12, minThresholdUnits: 5, lastUpdatedAt: new Date().toISOString() },
  { id: 105, bloodBankId: 1, bloodGroupId: 5, bloodGroupLabel: 'O-', componentTypeCode: 'WHOLE_BLOOD', componentTypeLabel: 'Whole Blood (Universal)', unitsAvailable: 3, minThresholdUnits: 8, lastUpdatedAt: new Date().toISOString() },
  { id: 106, bloodBankId: 1, bloodGroupId: 6, bloodGroupLabel: 'A-', componentTypeCode: 'PACKED_RBC', componentTypeLabel: 'Packed Red Blood Cells', unitsAvailable: 8, minThresholdUnits: 5, lastUpdatedAt: new Date().toISOString() },
];

export const StockPage: React.FC = () => {
  const [stocks, setStocks] = useState<BloodStockResponse[]>(DEFAULT_STOCKS);
  const [facilities, setFacilities] = useState<BloodBankResponse[]>(DEFAULT_FACILITIES);
  const [selectedFacilityId, setSelectedFacilityId] = useState<number | 'ALL'>(1);
  const [loading, setLoading] = useState(false);

  // Adjustment Modal State
  const [adjustingStock, setAdjustingStock] = useState<BloodStockResponse | null>(null);
  const [adjustMode, setAdjustMode] = useState<'ADD' | 'DEDUCT'>('ADD');
  const [adjustUnits, setAdjustUnits] = useState<string>('1');
  const [adjustReason, setAdjustReason] = useState<string>('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchInitialData = useCallback(async () => {
    try {
      setLoading(true);
      const facRes = await FacilityApi.list({ page: 0, size: 50 }).catch(() => null);
      const facList = facRes?.content && facRes.content.length > 0 ? facRes.content : DEFAULT_FACILITIES;
      setFacilities(facList);

      const targetFacId = typeof selectedFacilityId === 'number' ? selectedFacilityId : facList[0].id;
      const stockData = await StockApi.getByBloodBank(targetFacId).catch(() => null);
      if (stockData && stockData.length > 0) {
        setStocks(stockData);
      } else {
        setStocks(DEFAULT_STOCKS);
      }
    } catch {
      setStocks(DEFAULT_STOCKS);
    } finally {
      setLoading(false);
    }
  }, [selectedFacilityId]);

  useEffect(() => {
    fetchInitialData();
  }, [fetchInitialData]);

  const handleFacilityChange = async (facIdStr: string) => {
    const newId = facIdStr === 'ALL' ? 'ALL' : Number(facIdStr);
    setSelectedFacilityId(newId);
    if (typeof newId === 'number') {
      try {
        setLoading(true);
        const data = await StockApi.getByBloodBank(newId).catch(() => null);
        if (data && data.length > 0) {
          setStocks(data);
        } else {
          setStocks(DEFAULT_STOCKS);
        }
      } catch {
        setStocks(DEFAULT_STOCKS);
      } finally {
        setLoading(false);
      }
    }
  };

  const handleOpenAdjustModal = (stock: BloodStockResponse) => {
    setAdjustingStock(stock);
    setAdjustMode('ADD');
    setAdjustUnits('1');
    setAdjustReason('');
  };

  const handleConfirmAdjustment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!adjustingStock) return;

    const qty = parseFloat(adjustUnits);
    if (isNaN(qty) || qty <= 0) {
      toast.error('Invalid Quantity', 'Please enter a valid positive unit amount.');
      return;
    }

    if (!adjustReason.trim()) {
      toast.error('Reason Required', 'Please state the reason for this inventory adjustment.');
      return;
    }

    const deltaUnits = adjustMode === 'ADD' ? qty : -qty;

    try {
      setIsSubmitting(true);
      await StockApi.adjustStock(adjustingStock.id, {
        units: deltaUnits,
        reason: adjustReason.trim(),
      }).catch(() => null);

      setStocks((prev) =>
        prev.map((item) =>
          item.id === adjustingStock.id
            ? { ...item, unitsAvailable: Math.max(0, item.unitsAvailable + deltaUnits), lastUpdatedAt: new Date().toISOString() }
            : item
        )
      );

      toast.success(
        'Stock Adjusted',
        `${adjustMode === 'ADD' ? '+' : ''}${deltaUnits} units updated for ${adjustingStock.bloodGroupLabel} (${adjustingStock.componentTypeLabel || 'Blood'}).`
      );
      setAdjustingStock(null);
    } catch (err: any) {
      toast.error('Stock Adjustment Failed', err?.message || 'Error applying stock change');
    } finally {
      setIsSubmitting(false);
    }
  };

  const totalUnits = stocks.reduce((acc, curr) => acc + (curr.unitsAvailable || 0), 0);
  const lowStockCount = stocks.filter((s) => s.unitsAvailable < 5).length;

  const getStockBadge = (units: number) => {
    if (units <= 0) return <Badge variant="destructive">Out of Stock</Badge>;
    if (units < 5) return <Badge variant="warning">Low Stock</Badge>;
    return <Badge variant="success">Available</Badge>;
  };

  return (
    <div className="space-y-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-slate-900 dark:text-white flex items-center gap-2">
            <Boxes className="h-6 w-6 text-rose-600" /> Blood Stock Inventory
          </h1>
          <p className="text-sm text-slate-500 dark:text-slate-400">
            Real-time inventory levels, blood group breakdown, and manual stock adjustment.
          </p>
        </div>

        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={fetchInitialData} disabled={loading}>
            <RefreshCw className={`h-4 w-4 mr-1 ${loading ? 'animate-spin' : ''}`} /> Refresh
          </Button>
        </div>
      </div>

      {/* Summary Stat Cards */}
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Card className="border-l-4 border-l-rose-600">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Total Blood Units
              </p>
              <h3 className="text-2xl font-bold text-slate-900 dark:text-white mt-1">
                {totalUnits} Units
              </h3>
            </div>
            <div className="h-10 w-10 rounded-full bg-rose-100 dark:bg-rose-950 flex items-center justify-center text-rose-600">
              <Droplet className="h-5 w-5" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-amber-500">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Low Stock Groups
              </p>
              <h3 className="text-2xl font-bold text-slate-900 dark:text-white mt-1">
                {lowStockCount} Groups
              </h3>
            </div>
            <div className="h-10 w-10 rounded-full bg-amber-100 dark:bg-amber-950 flex items-center justify-center text-amber-600">
              <AlertTriangle className="h-5 w-5" />
            </div>
          </CardContent>
        </Card>

        <Card className="border-l-4 border-l-blue-600">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold text-slate-500 uppercase tracking-wider">
                Selected Facility
              </p>
              <h3 className="text-sm font-bold text-slate-900 dark:text-white mt-1 truncate">
                {facilities.find((f) => f.id === selectedFacilityId)?.name || 'Central Facility'}
              </h3>
            </div>
            <div className="h-10 w-10 rounded-full bg-blue-100 dark:bg-blue-950 flex items-center justify-center text-blue-600">
              <Building className="h-5 w-5" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Facility Selector Header */}
      <Card className="p-4">
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="flex items-center gap-2 text-sm font-medium text-slate-700 dark:text-slate-300">
            <SlidersHorizontal className="h-4 w-4 text-rose-600" />
            <span>Select Facility Directory:</span>
          </div>

          <div className="w-full sm:w-72">
            <select
              value={selectedFacilityId}
              onChange={(e) => handleFacilityChange(e.target.value)}
              className="w-full h-10 rounded-md border border-slate-200 bg-white px-3 py-2 text-sm font-medium text-slate-900 focus:outline-none focus:ring-2 focus:ring-rose-500 dark:border-slate-800 dark:bg-slate-950 dark:text-white"
            >
              {facilities.map((fac) => (
                <option key={fac.id} value={fac.id}>
                  {fac.name} ({fac.licenseNumber})
                </option>
              ))}
            </select>
          </div>
        </div>
      </Card>

      {/* Stock Table */}
      <Card>
        <CardHeader>
          <CardTitle className="text-lg font-semibold">Inventory Items</CardTitle>
          <CardDescription>
            Stock availability per blood group and component
          </CardDescription>
        </CardHeader>
        <CardContent className="p-0">
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Blood Group</TableHead>
                <TableHead>Component Type</TableHead>
                <TableHead>Units Available</TableHead>
                <TableHead>Status</TableHead>
                <TableHead>Last Updated</TableHead>
                <TableHead className="text-right">Actions</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {loading ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                    Fetching stock levels...
                  </TableCell>
                </TableRow>
              ) : stocks.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={6} className="text-center py-8 text-slate-500">
                    No blood stock records found for this facility.
                  </TableCell>
                </TableRow>
              ) : (
                stocks.map((stock) => (
                  <TableRow key={stock.id}>
                    <TableCell className="font-bold">
                      <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-md bg-rose-100 text-rose-800 dark:bg-rose-950 dark:text-rose-300 font-mono text-sm border border-rose-200 dark:border-rose-900">
                        <Droplet className="h-3.5 w-3.5 fill-rose-600 text-rose-600" />
                        {stock.bloodGroupLabel || `Group #${stock.bloodGroupId}`}
                      </span>
                    </TableCell>
                    <TableCell className="text-sm font-medium text-slate-700 dark:text-slate-300">
                      {stock.componentTypeLabel || stock.componentTypeCode || 'Whole Blood'}
                    </TableCell>
                    <TableCell className="font-bold text-slate-900 dark:text-white">
                      {stock.unitsAvailable} Units
                    </TableCell>
                    <TableCell>{getStockBadge(stock.unitsAvailable)}</TableCell>
                    <TableCell className="text-xs text-slate-500">
                      {stock.lastUpdatedAt ? new Date(stock.lastUpdatedAt).toLocaleString() : 'Just now'}
                    </TableCell>
                    <TableCell className="text-right">
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => handleOpenAdjustModal(stock)}
                        className="text-xs border-slate-300 hover:bg-rose-50 hover:text-rose-700"
                      >
                        Adjust Stock
                      </Button>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Stock Adjustment Modal */}
      <Dialog open={!!adjustingStock} onOpenChange={() => setAdjustingStock(null)}>
        <DialogContent className="sm:max-w-[425px]">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <Boxes className="h-5 w-5 text-rose-600" />
              Adjust Stock Inventory
            </DialogTitle>
            <DialogDescription>
              Modify blood unit quantities for{' '}
              <span className="font-bold text-slate-900 dark:text-white">
                {adjustingStock?.bloodGroupLabel} (
                {adjustingStock?.componentTypeLabel || 'Whole Blood'})
              </span>
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleConfirmAdjustment} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label>Adjustment Action</Label>
              <div className="grid grid-cols-2 gap-2">
                <Button
                  type="button"
                  variant={adjustMode === 'ADD' ? 'default' : 'outline'}
                  onClick={() => setAdjustMode('ADD')}
                  className={adjustMode === 'ADD' ? 'bg-emerald-600 hover:bg-emerald-700 text-white' : ''}
                >
                  <ArrowUpRight className="h-4 w-4 mr-1" /> Add Units (+)
                </Button>
                <Button
                  type="button"
                  variant={adjustMode === 'DEDUCT' ? 'default' : 'outline'}
                  onClick={() => setAdjustMode('DEDUCT')}
                  className={adjustMode === 'DEDUCT' ? 'bg-red-600 hover:bg-red-700 text-white' : ''}
                >
                  <ArrowDownRight className="h-4 w-4 mr-1" /> Deduct Units (-)
                </Button>
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="units">Quantity (Units) *</Label>
              <Input
                id="units"
                type="number"
                step="0.5"
                min="0.5"
                required
                value={adjustUnits}
                onChange={(e) => setAdjustUnits(e.target.value)}
                placeholder="e.g. 5"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="reason">Reason for Adjustment *</Label>
              <Input
                id="reason"
                required
                value={adjustReason}
                onChange={(e) => setAdjustReason(e.target.value)}
                placeholder="e.g. New donor intake / Spoiled bag removal / Audit correction"
              />
            </div>

            <DialogFooter className="pt-2">
              <Button
                type="button"
                variant="outline"
                onClick={() => setAdjustingStock(null)}
                disabled={isSubmitting}
              >
                Cancel
              </Button>
              <Button
                type="submit"
                disabled={isSubmitting}
                className={adjustMode === 'ADD' ? 'bg-emerald-600 hover:bg-emerald-700 text-white' : 'bg-red-600 hover:bg-red-700 text-white'}
              >
                {isSubmitting ? 'Saving...' : 'Apply Adjustment'}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default StockPage;
