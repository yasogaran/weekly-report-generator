"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { ReactNode } from "react";
import Tooltip from "@/components/ui/Tooltip";

interface NavItemProps {
  href: string;
  icon: ReactNode;
  label: string;
  /** Icon-only rendering for Sidebar's collapsed state. */
  collapsed?: boolean;
}

// Single nav link — active state uses the accent token per the design doc's color table
// ("accent: Primary actions, active nav, links"), not a one-off color.
export default function NavItem({ href, icon, label, collapsed = false }: NavItemProps) {
  const pathname = usePathname();
  const isActive = pathname === href;

  const link = (
    <Link
      href={href}
      aria-current={isActive ? "page" : undefined}
      aria-label={collapsed ? label : undefined}
      className={`flex items-center gap-2 rounded px-3 py-2 text-sm font-medium transition-colors ${
        collapsed ? "justify-center" : ""
      } ${isActive ? "bg-accent/10 text-accent" : "text-ink hover:bg-line/40"}`}
    >
      {icon}
      {!collapsed && label}
    </Link>
  );

  // Collapsed = icon with no visible label — the design doc requires a Tooltip in exactly
  // this situation ("every icon-only button... must have both a Tooltip and an aria-label").
  // A NavItem is a link, not a Button, so it can't get that enforcement structurally the way
  // Button's ghost variant does; wrapping it here is what actually satisfies the rule.
  if (!collapsed) {
    return link;
  }
  return <Tooltip content={label}>{link}</Tooltip>;
}
