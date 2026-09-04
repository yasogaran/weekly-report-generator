"use client";

import { useEffect, useState } from "react";
import {
  ClipboardList,
  FilePlus,
  FolderKanban,
  History,
  LayoutDashboard,
  PanelLeftClose,
  PanelLeftOpen,
  Users as UsersIcon,
} from "lucide-react";
import Button from "@/components/ui/Button";
import Tooltip from "@/components/ui/Tooltip";
import LogoutButton from "./LogoutButton";
import NavItem from "./NavItem";
import { UserRole, useAuth } from "@/lib/authContext";

interface NavItemSpec {
  href: string;
  label: string;
  icon: typeof FilePlus;
}

// One role-aware component, not two near-duplicate sidebars (CLAUDE.md) — the nav list is
// the only thing that varies by role, so it's the only thing keyed by role here.
const NAV_ITEMS_BY_ROLE: Record<UserRole, NavItemSpec[]> = {
  TEAM_MEMBER: [
    { href: "/reports/new", label: "New Report", icon: FilePlus },
    { href: "/history", label: "Report History", icon: History },
  ],
  MANAGER: [
    { href: "/dashboard", label: "Dashboard", icon: LayoutDashboard },
    // Sits right after Dashboard — both are report-oriented (Dashboard: aggregate metrics,
    // Reports: the actual queue to click into), before the more admin-flavored Projects/Users.
    { href: "/reports", label: "Reports", icon: ClipboardList },
    { href: "/projects", label: "Projects", icon: FolderKanban },
    { href: "/users", label: "Users", icon: UsersIcon },
  ],
};

const ROLE_LABEL: Record<UserRole, string> = {
  TEAM_MEMBER: "Team Member",
  MANAGER: "Manager",
};

const COLLAPSED_STORAGE_KEY = "wrg_sidebar_collapsed";

// Fixed-width left sidebar per frontend-design-system.md §3's layout reference: logo/app
// name + collapse toggle at top, role-aware nav list, user identity + logout pinned to the
// bottom. Reads the current user directly via useAuth() rather than taking it as a prop,
// since every caller (both role layouts) already sits inside AuthProvider and would just be
// passing through the same hook's result.
export default function Sidebar() {
  const { currentUser } = useAuth();

  // Starts false on every render (server and the client's first pass alike) rather than a
  // lazy initializer reading localStorage — reading it eagerly would make the client's
  // first render disagree with the server-rendered HTML (which has no localStorage access
  // at all) and trigger a hydration mismatch, the same class of bug fixed in
  // lib/authContext.tsx for currentUser itself. The actual stored preference is applied a
  // tick later, after hydration has already reconciled.
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    // eslint-disable-next-line react-hooks/set-state-in-effect -- deliberate, see comment above.
    setCollapsed(localStorage.getItem(COLLAPSED_STORAGE_KEY) === "true");
  }, []);

  const toggleCollapsed = () => {
    setCollapsed((prev) => {
      const next = !prev;
      localStorage.setItem(COLLAPSED_STORAGE_KEY, String(next));
      return next;
    });
  };

  // The layouts that render this already gate on currentUser being loaded and valid before
  // reaching this point (see (manager)/layout.tsx's role check, and AuthProvider's loading
  // state generally) — this is just a defensive fallback, not the real guard.
  if (!currentUser) {
    return null;
  }

  const items = NAV_ITEMS_BY_ROLE[currentUser.role];

  return (
    <aside
      className={`flex h-screen shrink-0 flex-col overflow-y-auto border-r border-line bg-paper transition-[width] duration-200 ease-in-out ${
        collapsed ? "w-16" : "w-56"
      }`}
    >
      <div className={`flex items-center px-4 py-5 ${collapsed ? "justify-center" : "justify-between"}`}>
        {!collapsed && <span className="text-base font-semibold text-ink">Weekly Reports</span>}
        <Button
          variant="ghost"
          ariaLabel={collapsed ? "Expand sidebar" : "Collapse sidebar"}
          icon={collapsed ? <PanelLeftOpen size={18} /> : <PanelLeftClose size={18} />}
          onClick={toggleCollapsed}
        />
      </div>

      <nav className={`flex flex-1 flex-col gap-1 px-3 ${collapsed ? "items-center" : ""}`}>
        {items.map((item) => (
          <NavItem
            key={item.href}
            href={item.href}
            icon={<item.icon size={18} />}
            label={item.label}
            collapsed={collapsed}
          />
        ))}
      </nav>

      <div className="flex flex-col items-center gap-3 border-t border-line px-4 py-4">
        {collapsed ? (
          // Collapsed footer: just an initial avatar (tooltip carries the full name + role,
          // same pattern NavItem uses for its labels) plus the icon-only LogoutButton — the
          // full name/role text simply has nowhere to fit at this width, and truncating it
          // to one letter without a tooltip would leave no way to confirm which account is
          // logged in.
          <Tooltip content={`${currentUser.name} — ${ROLE_LABEL[currentUser.role]}`}>
            <div className="flex h-8 w-8 items-center justify-center rounded-full bg-accent/10 text-sm font-semibold text-accent">
              {currentUser.name.charAt(0).toUpperCase()}
            </div>
          </Tooltip>
        ) : (
          <div className="w-full">
            <p className="truncate text-sm font-medium text-ink">{currentUser.name}</p>
            <p className="text-xs text-muted">{ROLE_LABEL[currentUser.role]}</p>
          </div>
        )}
        <LogoutButton collapsed={collapsed} />
      </div>
    </aside>
  );
}
