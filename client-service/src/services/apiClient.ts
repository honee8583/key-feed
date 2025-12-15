import { clearStoredAuth, getAccessToken, getStoredAuth, saveStoredAuth, type StoredAuthState } from './authStorage'

type RequestBody = BodyInit | Record<string, unknown> | undefined

export type ApiRequestOptions = Omit<RequestInit, 'body'> & {
  body?: RequestBody
}

class ApiClient {
  private readonly baseUrl: string
  private refreshPromise: Promise<boolean> | null = null

  constructor(baseUrl = '') {
    this.baseUrl = baseUrl.replace(/\/$/, '')
  }

  async request<T>(path: string, options: ApiRequestOptions = {}, allowRetryOnUnauthorized = true): Promise<T> {
    const url = `${this.baseUrl}${path}`
    const headers = new Headers(options.headers)
    const body = this.prepareBody(options.body, headers)
    this.applyAuthHeader(headers)

    const response = await fetch(url, {
      ...options,
      headers,
      body,
      credentials: options.credentials ?? 'include',
    })

    if (!response.ok) {
      if (response.status === 401 && allowRetryOnUnauthorized) {
        const refreshed = await this.tryRefreshAccessToken()
        if (refreshed) {
          return this.request<T>(path, options, false)
        }
      }
      throw new Error(await this.buildErrorMessage(response))
    }

    return parseResponseBody<T>(response)
  }

  private prepareBody(body: RequestBody, headers: Headers) {
    if (!body || body instanceof FormData || typeof body === 'string') {
      return body
    }

    headers.set('Content-Type', 'application/json')
    return JSON.stringify(body)
  }

  private async buildErrorMessage(response: Response) {
    try {
      const text = await response.text()
      if (!text) {
        return `요청에 실패했습니다. (status: ${response.status})`
      }
      const data = parseJsonWithLargeIntSupport(text)
      if (data && typeof data === 'object' && 'message' in data) {
        const message = (data as { message?: unknown }).message
        if (typeof message === 'string' && message) {
          return message
        }
      }
    } catch (error) {
      console.warn('Failed to parse error response', error)
    }
    return `요청에 실패했습니다. (status: ${response.status})`
  }

  private applyAuthHeader(headers: Headers) {
    if (headers.has('Authorization')) {
      return
    }
    const token = getAccessToken()
    if (token) {
      headers.set('Authorization', `Bearer ${token}`)
    }
  }

  private async tryRefreshAccessToken() {
    if (!getStoredAuth()) {
      return false
    }

    if (!this.refreshPromise) {
      this.refreshPromise = this.refreshAccessToken().finally(() => {
        this.refreshPromise = null
      })
    }

    return this.refreshPromise
  }

  private async refreshAccessToken(): Promise<boolean> {
    if (!getStoredAuth()) {
      return false
    }

    try {
      const response = await fetch(`${this.baseUrl}/auth/refresh`, {
        method: 'POST',
        credentials: 'include',
      })

      if (!response.ok) {
        clearStoredAuth()
        return false
      }

      const nextToken = await this.extractAccessToken(response)
      if (!nextToken) {
        clearStoredAuth()
        return false
      }

      const latestAuth = getStoredAuth()
      if (!latestAuth) {
        return false
      }

      const nextState: StoredAuthState = {
        ...latestAuth,
        token: nextToken,
      }
      saveStoredAuth(nextState)
      return true
    } catch (error) {
      console.error('Failed to refresh access token', error)
      clearStoredAuth()
      return false
    }
  }

  private async extractAccessToken(response: Response): Promise<string | null> {
    const contentType = response.headers.get('content-type') ?? ''
    if (!contentType.includes('application/json')) {
      const text = await response.text()
      try {
        const parsed = JSON.parse(text)
        return this.resolveAccessTokenFromPayload(parsed)
      } catch (error) {
        console.warn('Unexpected refresh response', error)
        return null
      }
    }

    const data = await response.json()
    return this.resolveAccessTokenFromPayload(data)
  }

  private resolveAccessTokenFromPayload(payload: unknown): string | null {
    if (!payload || typeof payload !== 'object') {
      return null
    }

    const directToken = (payload as { accessToken?: unknown }).accessToken
    if (typeof directToken === 'string' && directToken) {
      return directToken
    }

    const nestedToken = (payload as { data?: { accessToken?: unknown } }).data?.accessToken
    if (typeof nestedToken === 'string' && nestedToken) {
      return nestedToken
    }

    return null
  }
}

const resolvedBaseUrl =
  (import.meta.env.VITE_API_BASE && import.meta.env.VITE_API_BASE.trim()) || 'http://localhost:8000/api'

export const apiClient = new ApiClient(resolvedBaseUrl)

async function parseResponseBody<T>(response: Response): Promise<T> {
  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) {
    return undefined as T
  }

  const contentType = response.headers.get('content-type') ?? ''
  if (contentType.includes('application/json')) {
    return parseJsonWithLargeIntSupport(text) as T
  }

  return text as T
}

export function parseJsonWithLargeIntSupport(text: string) {
  const sanitized = text.replace(/(:\s*)(-?\d{16,})(\s*[,\}\]])/g, (_match, prefix, digits, suffix) => {
    return `${prefix}"${digits}"${suffix}`
  })
  try {
    return JSON.parse(sanitized)
  } catch (error) {
    console.warn('Failed to parse JSON with large integer support; falling back to default parser.', error)
    return JSON.parse(text)
  }
}
