export function getLandingRouteForRoles(roles: string[] = []): string {
  const normalizedRoles = roles.map((r) => r.toLowerCase());

  if (normalizedRoles.includes('admin') || normalizedRoles.includes('role_admin')) {
    return '/inventory';
  }
  if (normalizedRoles.includes('hospital_staff') || normalizedRoles.includes('role_hospital_staff')) {
    return '/requests';
  }
  if (normalizedRoles.includes('blood_bank_staff') || normalizedRoles.includes('role_blood_bank_staff')) {
    return '/inventory';
  }
  return '/inventory';
}
