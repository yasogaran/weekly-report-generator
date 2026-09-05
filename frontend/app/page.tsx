"use client";

import Image from "next/image";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect } from "react";
import { ClipboardList, LayoutDashboard, RefreshCw, ShieldCheck } from "lucide-react";
import Button from "@/components/ui/Button";
import { useAuth } from "@/lib/authContext";
import icon from "./icon.png";

const FEATURES = [
  {
    icon: ClipboardList,
    title: "Structured weekly reporting",
    description: "Log tasks, hours by type, achievements, and blockers in the same format every week.",
  },
  {
    icon: RefreshCw,
    title: "Review and correction workflow",
    description: "Managers approve or send a report back with a comment; you revise and resubmit without losing history.",
  },
  {
    icon: LayoutDashboard,
    title: "Team dashboard and analytics",
    description: "Managers see submission trends, hours breakdowns, and status across the whole team at a glance.",
  },
  {
    icon: ShieldCheck,
    title: "Role-based access",
    description: "Team members only ever see their own reports; managers review and never edit report content directly.",
  },
];

// "use client": needs useAuth() to redirect an already-logged-in visitor before rendering
// any landing content — see the (manager)/layout.tsx role-guard for the same "render
// nothing until the check resolves" pattern this follows.
export default function Home() {
  const { currentUser, loading } = useAuth();
  const router = useRouter();

  useEffect(() => {
    if (!loading && currentUser) {
      router.replace(currentUser.role === "MANAGER" ? "/dashboard" : "/history");
    }
  }, [loading, currentUser, router]);

  // While auth state is still resolving, or once we know someone's logged in and the
  // redirect above is about to fire, render nothing — a logged-in visitor should never see
  // this page's content even for a frame.
  if (loading || currentUser) {
    return null;
  }

  return (
    <main className="flex flex-1 items-center justify-center p-6">
      <div className="flex w-full max-w-3xl flex-col items-center gap-12 py-12 text-center">
        <div className="flex flex-col items-center gap-4">
          <Image src={icon} alt="" width={48} height={48} priority />
          <div>
            <h1 className="text-[28px] font-semibold text-ink">Weekly Reports</h1>
            <p className="mt-2 text-base text-muted">
              Weekly reports, review, and team insight in one place.
            </p>
          </div>
        </div>

        <div className="grid w-full grid-cols-1 gap-6 text-left sm:grid-cols-2">
          {FEATURES.map((feature) => (
            <div key={feature.title} className="flex flex-col gap-2 rounded border border-line p-4">
              <feature.icon size={20} className="text-accent" aria-hidden="true" />
              <h2 className="text-sm font-semibold text-ink">{feature.title}</h2>
              <p className="text-sm text-muted">{feature.description}</p>
            </div>
          ))}
        </div>

        <Link href="/login">
          <Button variant="primary">Log in</Button>
        </Link>
      </div>
    </main>
  );
}
