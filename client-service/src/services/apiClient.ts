import {
  clearStoredAuth,
  getAccessToken,
  getStoredAuth,
  saveStoredAuth,
  type StoredAuthState,
} from "./authStorage";

type RequestBody = BodyInit | Record<string, unknown> | undefined;

export type ApiRequestOptions = Omit<RequestInit, "body"> & {
  body?: RequestBody;
};

export class ApiError extends Error {
  status: number;
  data?: unknown;

  constructor(
    status: number,
    message: string,
    data?: unknown
  ) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.data = data;
  }
}

class ApiClient {
  private readonly baseUrl: string;
  private refreshPromise: Promise<boolean> | null = null;

  constructor(baseUrl = "") {
    this.baseUrl = baseUrl.replace(/\/$/, "");
  }

  async request<T>(
    path: string,
    options: ApiRequestOptions = {},
    allowRetryOnUnauthorized = true
  ): Promise<T> {
    const url = `${this.baseUrl}${path}`;
    const headers = new Headers(options.headers);
    const body = this.prepareBody(options.body, headers);
    this.applyAuthHeader(headers);

    const response = await fetch(url, {
      ...options,
      headers,
      body,
      credentials: options.credentials ?? "include",
    });

    if (!response.ok) {
      if (response.status === 401 && allowRetryOnUnauthorized) {
        const refreshed = await this.tryRefreshAccessToken();
        if (refreshed) {
          return this.request<T>(path, options, false);
        }
        // 리프레시 토큰도 실패했으면 인증이 만료된 것이므로 로그인 페이지로 리다이렉트
        this.handleAuthenticationFailure();
      }
      
      const errorMessage = await this.buildErrorMessage(response);
      throw new ApiError(response.status, errorMessage);
    }

    return parseResponseBody<T>(response);
  }

  private prepareBody(body: RequestBody, headers: Headers) {
    if (!body || body instanceof FormData || typeof body === "string") {
      return body;
    }

    headers.set("Content-Type", "application/json");
    return JSON.stringify(body);
  }

  private async buildErrorMessage(response: Response) {
    try {
      const text = await response.text();
      if (!text) {
        return `요청에 실패했습니다. (status: ${response.status})`;
      }
      const data = parseJsonWithLargeIntSupport(text);
      if (data && typeof data === "object" && "message" in data) {
        const message = (data as { message?: unknown }).message;
        if (typeof message === "string" && message) {
          return message;
        }
      }
    } catch (error) {
      console.warn("Failed to parse error response", error);
    }
    return `요청에 실패했습니다. (status: ${response.status})`;
  }

  private applyAuthHeader(headers: Headers) {
    if (headers.has("Authorization")) {
      return;
    }
    const token = getAccessToken();
    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }
  }

  private async tryRefreshAccessToken() {
    if (!getStoredAuth()) {
      return false;
    }

    if (!this.refreshPromise) {
      this.refreshPromise = this.refreshAccessToken().finally(() => {
        this.refreshPromise = null;
      });
    }

    return this.refreshPromise;
  }

  private async refreshAccessToken(): Promise<boolean> {
    if (!getStoredAuth()) {
      return false;
    }

    try {
      const response = await fetch(`${this.baseUrl}/auth/refresh`, {
        method: "POST",
        credentials: "include",
      });

      if (!response.ok) {
        console.error(`Refresh token failed with status: ${response.status}`);
        // 401(Unauthorized)이나 403(Forbidden)일 때만 로그아웃 처리
        // 5xx 서버 에러나 네트워크 에러 등에서는 로그아웃 하지 않음
        if (response.status === 401 || response.status === 403) {
          clearStoredAuth();
        }
        return false;
      }

      const nextToken = await this.extractAccessToken(response);
      if (!nextToken) {
        console.error("Failed to extract access token from refresh response");
        clearStoredAuth();
        return false;
      }

      const latestAuth = getStoredAuth();
      if (!latestAuth) {
        return false;
      }

      const nextState: StoredAuthState = {
        ...latestAuth,
        token: nextToken,
      };
      saveStoredAuth(nextState);
      return true;
    } catch (error) {
      console.error("Failed to refresh access token", error);
      clearStoredAuth();
      return false;
    }
  }

  private handleAuthenticationFailure() {
    // 인증 실패 시 로그인 페이지로 리다이렉트
    // 단, 이미 로그인 페이지에 있거나 리다이렉트 중이면 무시
    if (typeof window !== 'undefined') {
      const currentPath = window.location.pathname;
      if (!currentPath.includes('/login') && !currentPath.includes('/signup')) {
        // 약간의 지연을 두어 상태 업데이트가 완료되도록 함
        setTimeout(() => {
          // ProtectedRoute가 리다이렉트를 처리할 수 있도록 replace 사용
          window.location.replace('/login');
        }, 100);
      }
    }
  }

  private async extractAccessToken(response: Response): Promise<string | null> {
    const contentType = response.headers.get("content-type") ?? "";
    if (!contentType.includes("application/json")) {
      const text = await response.text();
      try {
        const parsed = JSON.parse(text);
        return this.resolveAccessTokenFromPayload(parsed);
      } catch (error) {
        console.warn("Unexpected refresh response", error);
        return null;
      }
    }

    const data = await response.json();
    return this.resolveAccessTokenFromPayload(data);
  }

  private resolveAccessTokenFromPayload(payload: unknown): string | null {
    if (!payload || typeof payload !== "object") {
      return null;
    }

    // 1. data 필드가 바로 토큰 문자열인 경우 (현재 백엔드 응답 구조)
    const dataField = (payload as { data?: unknown }).data;
    if (typeof dataField === "string" && dataField) {
      return dataField;
    }

    // 2. accessToken 필드가 최상단에 있는 경우
    const directToken = (payload as { accessToken?: unknown }).accessToken;
    if (typeof directToken === "string" && directToken) {
      return directToken;
    }

    // 3. data 객체 안에 accessToken이 있는 경우
    const nestedToken = (payload as { data?: { accessToken?: unknown } }).data
      ?.accessToken;
    if (typeof nestedToken === "string" && nestedToken) {
      return nestedToken;
    }

    return null;
  }
}

export const API_BASE_URL =
  (import.meta.env.VITE_API_BASE && import.meta.env.VITE_API_BASE.trim()) ||
  "http://localhost:8000/api";

export const apiClient = new ApiClient(API_BASE_URL);

async function parseResponseBody<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T;
  }

  const text = await response.text();
  if (!text) {
    return undefined as T;
  }

  const contentType = response.headers.get("content-type") ?? "";
  if (contentType.includes("application/json")) {
    return parseJsonWithLargeIntSupport(text) as T;
  }

  return text as T;
}

export function parseJsonWithLargeIntSupport(text: string) {
  const sanitized = text.replace(
    /(:\s*)(-?\d{16,})(\s*[,}\]])/g,
    (_match, prefix, digits, suffix) => {
      return `${prefix}"${digits}"${suffix}`;
    }
  );
  try {
    return JSON.parse(sanitized);
  } catch (error) {
    console.warn(
      "Failed to parse JSON with large integer support; falling back to default parser.",
      error
    );
    return JSON.parse(text);
  }
}
