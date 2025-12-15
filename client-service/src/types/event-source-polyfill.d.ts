declare module 'event-source-polyfill' {
  export interface EventSourcePolyfillInit extends EventSourceInit {
    headers?: Record<string, string>
    proxy?: string
    https?: {
      rejectUnauthorized?: boolean
    }
  }

  export class EventSourcePolyfill extends EventSource {
    constructor(url: string | URL, eventSourceInitDict?: EventSourcePolyfillInit)
  }
}
