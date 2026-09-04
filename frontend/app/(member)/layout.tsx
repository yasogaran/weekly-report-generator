import { ReactNode } from "react";
import Sidebar from "@/components/layout/Sidebar";

// No role redirect here (unlike (manager)/layout.tsx) — a MANAGER viewing a team-member
// route isn't a privilege escalation, just an odd path, so there's nothing to defend
// against on this side. Previously hosted its own ad-hoc header (user name + LogoutButton,
// via useAuth()) directly in this file; that's now consolidated into Sidebar, which reads
// the user itself — this layout no longer needs useAuth() at all, or to be a client
// component, since Sidebar (a client component) is the only part that's interactive.
export default function MemberLayout({ children }: { children: ReactNode }) {
  // `h-screen overflow-hidden` (not `min-h-screen`) — see (manager)/layout.tsx's comment for
  // why this is the part that actually keeps Sidebar fixed while <main> scrolls
  // independently; a height fix on Sidebar alone doesn't work without this.
  return (
    <div className="flex h-screen overflow-hidden">
      <Sidebar />
      <main className="flex-1 overflow-y-auto p-6">{children}</main>
    </div>
  );
}
