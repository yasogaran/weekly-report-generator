"use client";

import { ReactNode, useEffect } from "react";
import { useRouter } from "next/navigation";
import Sidebar from "@/components/layout/Sidebar";
import { useAuth } from "@/lib/authContext";

// Defense in depth, not the real enforcement — middleware.ts already redirects unauthenticated
// requests away from here before this ever renders, and every manager-only endpoint this
// layout's pages call re-checks the role server-side via @PreAuthorize regardless of what this
// does. This exists to catch the case middleware can't: a TEAM_MEMBER who *is* logged in (so
// middleware's presence-only check passes) but has no business on a manager route — e.g. they
// bookmarked a manager URL from when a manager account shared their browser, or just guessed
// one. Without this, they'd briefly see this layout shell (not real data — pages fetch inside
// themselves and every fetch would 403) before any page-level check kicked in.
export default function ManagerLayout({ children }: { children: ReactNode }) {
  const { currentUser, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && currentUser && currentUser.role !== "MANAGER") {
      router.replace("/history");
    }
  }, [loading, currentUser, router]);

  // While we don't yet know the role, or once we know it's wrong, render nothing rather
  // than the manager shell (Sidebar included) — avoids a flash of manager-only chrome for a
  // team member. This early return runs before <Sidebar /> ever mounts, so adding Sidebar
  // here doesn't change when the role check takes effect.
  if (loading || !currentUser || currentUser.role !== "MANAGER") {
    return null;
  }

  // `h-screen overflow-hidden` (not `min-h-screen`) is the actual fix for keeping Sidebar
  // fixed while content scrolls. `min-h-screen` only sets a *minimum* — once page content
  // grows taller than the viewport, this wrapper itself grows past 100vh too, so the
  // *document* scrolls, not <main>, and Sidebar (stretched to match this now-taller
  // wrapper) scrolls away with everything else. `h-screen` pins the wrapper at exactly
  // 100vh regardless of content, and `overflow-hidden` stops it from growing past that even
  // if something inside overflows — which is what lets <main>'s own `overflow-y-auto`
  // actually engage as an independent scroll container instead of never being needed.
  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar />
      <main className="flex-1 overflow-y-auto p-6">{children}</main>
    </div>
  );
}
