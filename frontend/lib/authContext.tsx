"use client";

import {
  ReactNode,
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { TOKEN_STORAGE_KEY, apiRequest } from "./apiClient";

export type UserRole = "TEAM_MEMBER" | "MANAGER";

export interface CurrentUser {
  id: number;
  name: string;
  email: string;
  role: UserRole;
}

// Shape of AuthResponse from POST /api/auth/login (docs/api/api-doc.md). `createdAt` comes
// back too but nothing in this app currently needs it, so it's not modeled.
interface AuthResponse {
  token: string;
  user: CurrentUser;
}

interface AuthContextValue {
  currentUser: CurrentUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<CurrentUser>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

// Non-sensitive flag cookie, readable by middleware.ts, that only says "a session might
// exist" — never the token itself. Session storage stays localStorage-only per the design
// decision below; this cookie is purely so the Edge middleware (which can't read
// localStorage — that's browser-only, never sent with a request) has *something* to check
// before a page even renders. Flagging this explicitly since it's a small addition beyond
// what the task described: a real cookie, even a valueless one, wasn't in the original plan.
const SESSION_FLAG_COOKIE = "wrg_has_session";

function setSessionFlagCookie(present: boolean) {
  document.cookie = present
    ? `${SESSION_FLAG_COOKIE}=1; path=/; max-age=${60 * 60 * 24}; SameSite=Lax`
    : `${SESSION_FLAG_COOKIE}=; path=/; max-age=0`;
}

// Decodes a JWT's payload without verifying its signature — base64url-decode the middle
// segment and JSON.parse it. This is ONLY for populating `currentUser` so the UI (name in
// the header, role-based nav) doesn't flash empty while we decide what to show on page
// load. It is NEVER used for access control: a tampered or expired token decodes exactly
// as "successfully" as a real one, since nothing here checks the signature. Every actual
// protected action still goes through apiClient -> the backend, which re-validates the
// signature and re-checks the user's live `isActive` state on every request (api-doc.md).
// Worst case if this decoding is wrong or stale: the UI briefly shows the wrong name/role
// until the next API call 401s and logout() runs — never a security boundary.
//
// Trade-off vs. re-fetching the user from the server on mount: decoding avoids a network
// round trip and works offline-first for display purposes, at the cost of trusting
// unverified claims and the claims going stale if the user's profile changes server-side
// without a fresh login. Chose decode-only because this app has no "edit my profile"
// feature yet that would make that staleness visible.
function decodeJwtPayload(token: string): CurrentUser | null {
  try {
    const payloadSegment = token.split(".")[1];
    const base64 = payloadSegment.replace(/-/g, "+").replace(/_/g, "/");
    const claims = JSON.parse(atob(base64));
    if (
      typeof claims.id !== "number" ||
      typeof claims.email !== "string" ||
      (claims.role !== "TEAM_MEMBER" && claims.role !== "MANAGER")
    ) {
      return null;
    }
    return {
      id: claims.id,
      name: typeof claims.name === "string" ? claims.name : "",
      email: claims.email,
      role: claims.role,
    };
  } catch {
    return null;
  }
}

// Session persistence: the JWT lives in localStorage, not an httpOnly cookie. This matches
// the backend's stateless bearer-token scheme (docs/api/api-doc.md — "Authorization: Bearer
// <token>", no server-side session, no logout endpoint since JWT can't be revoked). The
// trade-off: a token in localStorage is readable by any script on the page (XSS exposure),
// where an httpOnly cookie wouldn't be — but this app has no server-rendered
// user-authored HTML to inject into, and moving to cookies would require backend changes
// (cookie issuance, CSRF handling) outside this task's scope. Don't switch this to cookies
// without deliberately revisiting that trade-off and updating the backend to match — it's a
// design decision, not an oversight.
export function AuthProvider({ children }: { children: ReactNode }) {
  // Starts at null/true on EVERY render pass, including the client's very first one — not
  // a lazy initializer reading localStorage. This used to be a lazy initializer (to avoid a
  // one-frame "logged out" flash), which caused a real hydration mismatch: Next.js
  // server-renders this component with no access to localStorage (currentUser: null), but a
  // lazy initializer runs during the client's first render too — before hydration
  // reconciles — so it would immediately produce a *different* currentUser than what the
  // server sent down, and React discards + re-renders the whole subtree client-side when it
  // notices (the "Hydration failed" warning, pointing at children like
  // (manager)/layout.tsx that branch on currentUser). Restoring the session in an effect
  // instead guarantees the first client render matches the server output exactly; the
  // actual restoration happens a tick later, after hydration is already reconciled.
  const [currentUser, setCurrentUser] = useState<CurrentUser | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem(TOKEN_STORAGE_KEY);
    if (token) {
      // Deliberate: this MUST NOT be set during the initial render (see comment above) —
      // the lint's usual advice ("compute it during render instead," e.g. a lazy useState
      // initializer) is exactly the pattern that caused the hydration bug this replaces.
      // eslint-disable-next-line react-hooks/set-state-in-effect
      setCurrentUser(decodeJwtPayload(token));
      setSessionFlagCookie(true);
    }
    setLoading(false);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const data = await apiRequest<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    localStorage.setItem(TOKEN_STORAGE_KEY, data.token);
    setSessionFlagCookie(true);
    setCurrentUser(data.user);
    return data.user;
  }, []);

  // There's no server-side logout endpoint (JWT is stateless — api-doc.md) — "logging out"
  // is purely a client-side act of discarding the token.
  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_STORAGE_KEY);
    setSessionFlagCookie(false);
    setCurrentUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ currentUser, loading, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

// Access to the auth API from any client component. Throws if called outside AuthProvider
// so a missing provider fails loudly during development, matching useToast/useConfirm.
export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return ctx;
}
