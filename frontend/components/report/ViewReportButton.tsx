import Link from "next/link";
import { Eye } from "lucide-react";
import Button from "@/components/ui/Button";
import { UserRole } from "@/lib/authContext";
import { getReportViewDestination } from "@/lib/reportNavigation";
import { ReportStatus } from "@/lib/reportTypes";

interface ViewReportButtonProps {
  reportId: number;
  status: ReportStatus;
  viewerRole: UserRole;
}

// The one "View" action cell, reused by every report list table (manager queue, team
// member's own history, and the Team Member Profile page's history table once it exists)
// instead of each table wiring up the status/role routing logic separately. Ghost + visible
// label, not icon-only — see components/ui/Button.tsx for why that needed a small type
// extension (icon-only ghost buttons couldn't carry visible text before this).
export default function ViewReportButton({ reportId, status, viewerRole }: ViewReportButtonProps) {
  return (
    <Link href={getReportViewDestination({ id: reportId, status }, viewerRole)}>
      <Button variant="ghost" icon={<Eye size={16} />}>
        View
      </Button>
    </Link>
  );
}
