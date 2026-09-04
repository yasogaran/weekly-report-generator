import { NextRequest, NextResponse } from "next/server";

// UX redirect only — this checks whether a non-sensitive "a session might exist" cookie is
// present, nothing more. It does NOT verify the JWT (can't — the token itself lives in
// localStorage, which never reaches the server), doesn't check expiry, and doesn't check
// role. Its only job is to keep a logged-out person from momentarily landing on a page
// shell that will immediately fail to load data. Every real permission decision — including
// "is this actually a valid, unexpired token" and "does this role/ownership allow this
// action" — happens server-side, per request, in the Spring Boot backend (see
// docs/api/api-doc.md and docs/rbac-matrix.md). A team member (or an attacker with no
// account at all) editing this file, clearing it, or forging the cookie gains nothing: the
// backend would still 401/403/404 every request.
const SESSION_FLAG_COOKIE = "wrg_has_session";

// Routes that don't require a session. (auth) pages plus the public marketing/home page —
// everything else, including the future (member)/* and (manager)/* pages (route groups
// don't appear in the URL, so they can't be matched by folder name here), requires one.
const PUBLIC_PATHS = ["/", "/login", "/register"];

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  if (PUBLIC_PATHS.includes(pathname)) {
    return NextResponse.next();
  }

  const hasSession = request.cookies.has(SESSION_FLAG_COOKIE);
  if (!hasSession) {
    const loginUrl = new URL("/login", request.url);
    return NextResponse.redirect(loginUrl);
  }

  return NextResponse.next();
}

// Skip all Next.js internals (not just static/image) and static assets — only app routes
// need this check. This must exclude the dev HMR websocket path too: Next 16's Turbopack
// dev server upgrades on /_next/hmr, and a narrower exclusion here (e.g. just
// `_next/static|_next/image`) lets middleware intercept that request and redirect it to
// /login when no session cookie is present — which breaks the WebSocket handshake and shows
// up as a repeating "WebSocket connection to 'ws://localhost:3000/_next/hmr?id=...' failed"
// in the browser console. Excluding the whole `_next/` prefix avoids this regardless of
// which internal path Next uses next.
export const config = {
  matcher: ["/((?!_next/|favicon.ico).*)"],
};
