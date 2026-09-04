// The single seam every API call in this app goes through. No component or hook should
// call `fetch` directly against the backend — that would scatter base-URL, auth-header,
// and error-shape handling across the codebase instead of keeping it in one place.

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

// Shared with lib/authContext.tsx so both files agree on where the JWT lives.
export const TOKEN_STORAGE_KEY = "wrg_token";

// Matches the GlobalExceptionHandler error shape documented in docs/api/api-doc.md —
// every non-2xx response from the backend has this exact shape.
export interface ApiErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

// Envelope for every paginated list endpoint (GET /api/reports, and others to come). The
// backend uses Spring Data's Pageable (docs/api/api-doc.md — "page, size (Spring
// Pageable)"), and this matches Spring Boot's default Page<T> JSON serialization. This
// exact shape isn't spelled out in api-doc.md itself, so it's an assumption based on the
// stack, not a documented contract — worth confirming against a real response the first
// time this is wired up to the running backend.
export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

// Thrown instead of a generic Error on any non-2xx response, so callers can branch on
// `error.status` (e.g. redirect to /login on 401) without parsing a message string.
export class ApiError extends Error {
  readonly status: number;
  readonly error: string;
  readonly path: string;

  constructor(body: ApiErrorResponse) {
    super(body.message);
    this.name = "ApiError";
    this.status = body.status;
    this.error = body.error;
    this.path = body.path;
  }
}

// Wraps fetch with the three things every call needs: the base URL, the bearer token (if
// one is stored), and JSON parsing in both the success and error case. `T` is the shape of
// the parsed success response — callers assert it since the backend contract (api-doc.md)
// isn't generated/validated here.
export async function apiRequest<T>(path: string, options: RequestInit = {}): Promise<T> {
  const headers = new Headers(options.headers);

  if (options.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  // localStorage only exists in the browser — guard for the rare case this runs during SSR.
  const token = typeof window !== "undefined" ? localStorage.getItem(TOKEN_STORAGE_KEY) : null;
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const response = await fetch(`${API_BASE_URL}${path}`, { ...options, headers });

  // Some responses (e.g. a future 204) may have no body — only parse if there's text to
  // parse, and don't let a non-JSON body (e.g. an HTML error page from a proxy in front of
  // the API) throw here instead of producing a proper ApiError below.
  const rawBody = await response.text();
  let parsedBody: unknown = null;
  if (rawBody) {
    try {
      parsedBody = JSON.parse(rawBody);
    } catch {
      parsedBody = null;
    }
  }

  if (!response.ok) {
    // If the body isn't the expected ErrorResponse shape (e.g. a proxy/network error page
    // slipped through instead of JSON from GlobalExceptionHandler), fall back to a shape we
    // can still throw rather than letting JSON.parse's own error mask the real HTTP status.
    const looksLikeErrorResponse =
      typeof parsedBody === "object" &&
      parsedBody !== null &&
      typeof (parsedBody as Record<string, unknown>).status === "number";

    const errorBody: ApiErrorResponse = looksLikeErrorResponse
      ? (parsedBody as ApiErrorResponse)
      : {
            timestamp: new Date().toISOString(),
            status: response.status,
            error: response.statusText || "Unknown error",
            message: "Unexpected response from the server.",
            path,
          };
    throw new ApiError(errorBody);
  }

  return parsedBody as T;
}
