"use client";

import { LogOut } from "lucide-react";
import { useRouter } from "next/navigation";
import Button from "@/components/ui/Button";
import { useAuth } from "@/lib/authContext";

interface LogoutButtonProps {
  /** Renders as a ghost icon-only button (for Sidebar's collapsed rail) instead of the
   * labeled secondary button. Same handleLogout either way — this only changes
   * presentation, not the logout logic itself. */
  collapsed?: boolean;
}

// Lives in Sidebar's footer; kept as its own component (not inlined there) so the actual
// logout action stays in one place regardless of how many ways it ends up presented.
export default function LogoutButton({ collapsed = false }: LogoutButtonProps) {
  const { logout } = useAuth();
  const router = useRouter();

  const handleLogout = () => {
    logout();
    router.push("/login");
  };

  if (collapsed) {
    return <Button variant="ghost" ariaLabel="Log out" icon={<LogOut size={18} />} onClick={handleLogout} />;
  }

  return (
    <Button variant="secondary" onClick={handleLogout}>
      Log out
    </Button>
  );
}
