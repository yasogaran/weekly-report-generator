"use client";

import { useEffect, useState } from "react";
import { UserX } from "lucide-react";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import Select from "@/components/ui/Select";
import { useConfirm } from "@/components/ui/ConfirmDialog";
import { useToast } from "@/components/ui/Toast";
import Button from "@/components/ui/Button";
import { ApiError, PageResponse, apiRequest } from "@/lib/apiClient";
import { UserRole, useAuth } from "@/lib/authContext";
import { FEEDBACK_COLORS, MUTED_COLOR } from "@/lib/chartColors";
import { UserDTO } from "@/lib/userTypes";

const PAGE_SIZE = 10;

const ROLE_OPTIONS: { value: UserRole; label: string }[] = [
  { value: "TEAM_MEMBER", label: "Team Member" },
  { value: "MANAGER", label: "Manager" },
];

// "use client": needs apiClient (JWT from localStorage) and useAuth (to know which row is
// the acting manager's own account).
export default function UsersPage() {
  const toast = useToast();
  const confirm = useConfirm();
  const { currentUser } = useAuth();

  const [page, setPage] = useState(1);
  const [data, setData] = useState<PageResponse<UserDTO> | null>(null);

  useEffect(() => {
    apiRequest<PageResponse<UserDTO>>(`/api/users?page=${page - 1}&size=${PAGE_SIZE}`)
      .then(setData)
      .catch(() => toast.error("Couldn't load users — check your connection and try again."));
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on page change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  // Applies a successful change to the currently-loaded page locally, from the value we
  // know we sent — rather than trusting a specific response body shape for
  // PATCH .../role and DELETE, neither of which has its response shape spelled out in
  // docs/api/api-doc.md (unlike the report endpoints, which document a ReportDTO response).
  const patchLocalUser = (userId: number, patch: Partial<UserDTO>) => {
    setData((prev) =>
      prev
        ? { ...prev, content: prev.content.map((user) => (user.id === userId ? { ...user, ...patch } : user)) }
        : prev
    );
  };

  const handleRoleChange = async (user: UserDTO, newRole: UserRole) => {
    if (newRole === user.role) return;

    const grantingManager = newRole === "MANAGER";
    const confirmed = await confirm({
      title: `Change ${user.name}'s role to ${grantingManager ? "Manager" : "Team Member"}?`,
      description: grantingManager
        ? "This will give manager access, including the team dashboard and report reviews."
        : "This will remove manager access, including the team dashboard and report reviews.",
      variant: "primary",
      confirmLabel: "Change role",
    });
    if (!confirmed) return;

    try {
      await apiRequest(`/api/users/${user.id}/role`, {
        method: "PATCH",
        body: JSON.stringify({ role: newRole }),
      });
      patchLocalUser(user.id, { role: newRole });
      toast.success("Role updated");
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't update this user's role — check your connection and try again."
      );
    }
  };

  const handleDeactivate = async (user: UserDTO) => {
    // Destructive-variant action goes through ConfirmDialog (frontend-design-system.md §2).
    // This exact framing is accurate per the backend behavior: soft delete, blocks future
    // login, invalidates already-issued tokens on next use, but reports/reviews they
    // authored are untouched (no cascade) — docs/api/api-doc.md, DELETE /api/users/{id}.
    const confirmed = await confirm({
      title: `Deactivate ${user.name}?`,
      description:
        "This user will no longer be able to log in. Their existing reports and reviews are not affected.",
      variant: "destructive",
      confirmLabel: "Deactivate",
    });
    if (!confirmed) return;

    try {
      await apiRequest(`/api/users/${user.id}`, { method: "DELETE" });
      patchLocalUser(user.id, { isActive: false });
      toast.success("User deactivated");
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't deactivate this user — check your connection and try again."
      );
    }
  };

  const columns: DataTableColumn<UserDTO>[] = [
    { key: "name", header: "Name", render: (row) => row.name },
    { key: "email", header: "Email", render: (row) => row.email },
    {
      key: "role",
      header: "Role",
      render: (row) => {
        const isSelf = currentUser?.id === row.id;
        // Hidden entirely rather than shown disabled — a manager can't change their own
        // role through this UI at all, so there's nothing useful a disabled control would
        // be explaining here that a plain label doesn't already say.
        if (isSelf) {
          return <span className="text-ink">{row.role === "MANAGER" ? "Manager" : "Team Member"} (you)</span>;
        }
        return (
          <Select
            value={row.role}
            onChange={(event) => handleRoleChange(row, event.target.value as UserRole)}
            options={ROLE_OPTIONS}
            className="w-40"
          />
        );
      },
    },
    {
      key: "status",
      header: "Status",
      render: (row) => (
        <span className="inline-flex items-center gap-1.5 text-ink">
          <span
            className="h-1.5 w-1.5 rounded-full"
            style={{ backgroundColor: row.isActive ? FEEDBACK_COLORS.success : MUTED_COLOR }}
            aria-hidden="true"
          />
          {row.isActive ? "Active" : "Deactivated"}
        </span>
      ),
    },
    {
      key: "actions",
      header: "",
      render: (row) => {
        const isSelf = currentUser?.id === row.id;
        // A manager can't deactivate their own account through this UI either — same
        // reasoning as the role column above, so the action is omitted, not disabled.
        if (isSelf || !row.isActive) return null;
        return (
          <div className="flex justify-end">
            <Button
              variant="ghost"
              ariaLabel="Deactivate user"
              icon={<UserX size={16} />}
              onClick={() => handleDeactivate(row)}
            />
          </div>
        );
      },
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <h1 className="text-[28px] font-semibold text-ink">Users</h1>
      <DataTable
        columns={columns}
        rows={data?.content ?? []}
        rowKey={(row) => row.id}
        pagination={
          data
            ? { page, totalPages: Math.max(data.totalPages, 1), onPageChange: setPage }
            : undefined
        }
      />
    </div>
  );
}
