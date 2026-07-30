export const API_BASE = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api/v1';

const TOKEN_KEY = 'bbms_access_token';

let spinnerShow: (() => void) | null = null;
let spinnerHide: (() => void) | null = null;

export function configureSpinner(show: () => void, hide: () => void) {
  spinnerShow = show;
  spinnerHide = hide;
}

export function getToken(): string | null {
  return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
}

export function setToken(token: string, remember: boolean = true): void {
  if (remember) {
    localStorage.setItem(TOKEN_KEY, token);
    sessionStorage.removeItem(TOKEN_KEY);
  } else {
    sessionStorage.setItem(TOKEN_KEY, token);
    localStorage.removeItem(TOKEN_KEY);
  }
}

export function removeToken(): void {
  localStorage.removeItem(TOKEN_KEY);
  sessionStorage.removeItem(TOKEN_KEY);
}

function generateTraceId(): string {
  return 'trace-' + Math.random().toString(36).substring(2, 11) + '-' + Date.now();
}

export async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const token = getToken();
  const traceId = generateTraceId();
  const headers: Record<string, string> = {
    'Content-Type': 'application/json',
    'X-Trace-Id': traceId,
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  if (spinnerShow) {
    spinnerShow();
  }

  try {
    const res = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });

    // Determine if current page or endpoint is public
    const publicPages = ['/login', '/register', '/verify-email', '/forgot-password', '/reset-password'];
    const isPublicPage = publicPages.some((p) => window.location.pathname.startsWith(p));
    const isPublicEndpoint = path.startsWith('/auth/') || path.startsWith('/master/');

    if (res.status === 401) {
      removeToken();
      localStorage.removeItem('bbms_user');
      if (!isPublicPage && !isPublicEndpoint) {
        window.location.href = '/login?expired=true';
      }
    }

    if (res.status === 204) {
      return undefined as T;
    }

    const json = await res.json().catch(() => null);

    if (!res.ok || (json && json.success === false)) {
      const errorMessage = json?.message ?? `HTTP ${res.status}: ${res.statusText}`;
      throw new Error(errorMessage);
    }

    // Unwrap envelope: return json.data if present, otherwise return json directly
    if (json && Object.prototype.hasOwnProperty.call(json, 'data')) {
      return json.data as T;
    }
    return json as T;
  } finally {
    if (spinnerHide) {
      spinnerHide();
    }
  }
}
