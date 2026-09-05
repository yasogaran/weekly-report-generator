"use client";

import { useEffect, useState } from "react";
import { Plus, UserX } from "lucide-react";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import Input from "@/components/ui/Input";
import Select from "@/components/ui/Select";
import { useConfirm } from "@/components/ui/ConfirmDialog";
import { useToast } from "@/components/ui/Toast";
import Button from "@/components/ui/Button";
import { ApiError, PageResponse, apiRequest } from "@/lib/apiClient";
import { UserRole, useAuth } from "@/lib/authContext";
import { FEEDBACK_COLORS, MUTED_COLOR } from "@/lib/chartColors";
import { UserDTO } from "@/lib/userTypes";

const PAGE_SIZE = 10;

// UX nicety only — same pattern the login page uses, not the real validation gate (the
// backend re-checks everything with @Valid on CreateUserRequest).
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const MIN_PASSWORD_LENGTH = 8;

const ROLE_OPTIONS: { value: UserRole; label: string }[] = [
  { value: "TEAM_MEMBER", label: "Team Member" },
  { value: "MANAGER", label: "Manager" },
];

interface NewUserForm {
  name: string;
  email: string;
  password: string;
  role: UserRole;
}

const EMPTY_NEW_USER: NewUserForm = { name: "", email: "", password: "", role: "TEAM_MEMBER" };

// "use client": needs apiClient (JWT from localStorage) and useAuth (to know which row is
// the acting manager's own account).
export default function UsersPage() {
  const toast = useToast();
  const confirm = useConfirm();
  const { currentUser } = useAuth();

  const [page, setPage] = useState(1);
  const [data, setData] = useState<PageResponse<UserDTO> | null>(null);

  // null = form closed, matching the add/edit-form pattern used on the Projects page.
  const [newUser, setNewUser] = useState<NewUserForm | null>(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    apiRequest<PageResponse<UserDTO>>(`/api/users?page=${page - 1}&size=${PAGE_SIZE}`)
      .then(setData)
      .catch(() => toast.error("Couldn't load users — check your connection and try again."));
    // `toast`'s identity changes every time a toast fires, which would re-trigger this
    // fetch in a loop on failure — deliberately excluded, refetch on page change only.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page]);

  const openCreateForm = () => setNewUser(EMPTY_NEW_USER);
  const closeCreateForm = () => setNewUser(null);

  const handleCreateUser = async () => {
    if (!newUser) return;

    // Required fields, basic email format, and a minimum password length — UX only. The
    // real enforcement is server-side (@Valid on CreateUserRequest), which is where this
    // would need to live for real in a production system rather than trusting the client.
    if (!newUser.name.trim() || !newUser.email.trim() || !newUser.password) {
      toast.error("Fill in all fields.");
      return;
    }
    if (!EMAIL_PATTERN.test(newUser.email)) {
      toast.error("Enter a valid email address.");
      return;
    }
    if (newUser.password.length < MIN_PASSWORD_LENGTH) {
      toast.error(`Password must be at least ${MIN_PASSWORD_LENGTH} characters.`);
      return;
    }

    setSubmitting(true);
    try {
      const created = await apiRequest<UserDTO>("/api/users", {
        method: "POST",
        body: JSON.stringify(newUser),
      });
      // Prepend locally rather than refetching — matches the role-change/deactivate pattern
      // on this page. New account shows up immediately even if it lands on a later page of
      // GET /api/users' pagination.
      setData((prev) => (prev ? { ...prev, content: [created, ...prev.content] } : prev));
      toast.success("User created");
      closeCreateForm();
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't create this user — check your connection and try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

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
      <div className="flex items-center justify-between">
        <h1 className="text-[28px] font-semibold text-ink">Users</h1>
        <Button variant="primary" onClick={openCreateForm}>
          <Plus size={16} />
          Add user
        </Button>
      </div>

      {newUser && (
        <div className="flex flex-col gap-3 rounded border border-line bg-surface p-4">
          <h2 className="text-sm font-semibold text-ink">New user</h2>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Name
            <Input
              value={newUser.name}
              onChange={(event) => setNewUser({ ...newUser, name: event.target.value })}
              autoComplete="name"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Email
            <Input
              type="email"
              value={newUser.email}
              onChange={(event) => setNewUser({ ...newUser, email: event.target.value })}
              autoComplete="email"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Password
            <Input
              type="password"
              value={newUser.password}
              onChange={(event) => setNewUser({ ...newUser, password: event.target.value })}
              autoComplete="new-password"
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Role
            <Select
              value={newUser.role}
              onChange={(event) => setNewUser({ ...newUser, role: event.target.value as UserRole })}
              options={ROLE_OPTIONS}
              className="w-40"
            />
          </label>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={closeCreateForm} disabled={submitting}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleCreateUser} disabled={submitting}>
              {submitting ? "Creating…" : "Create user"}
            </Button>
          </div>
        </div>
      )}

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
