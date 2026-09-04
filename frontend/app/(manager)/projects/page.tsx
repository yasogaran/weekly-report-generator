"use client";

import { useEffect, useState } from "react";
import { Pencil, Plus, Trash2 } from "lucide-react";
import Button from "@/components/ui/Button";
import DataTable, { DataTableColumn } from "@/components/ui/DataTable";
import Input from "@/components/ui/Input";
import Textarea from "@/components/ui/Textarea";
import { useConfirm } from "@/components/ui/ConfirmDialog";
import { useToast } from "@/components/ui/Toast";
import { ApiError, apiRequest } from "@/lib/apiClient";
import { ProjectDTO } from "@/lib/projectTypes";

// "use client": needs apiClient (JWT from localStorage) and owns the add/edit form's state.
export default function ProjectsPage() {
  const toast = useToast();
  const confirm = useConfirm();

  const [projects, setProjects] = useState<ProjectDTO[] | null>(null);
  // null = form closed. An object with `id: null` = creating; an object with a real `id` =
  // editing that project. One piece of state instead of separate "isOpen"/"editingId"
  // booleans, since the form only ever needs to know "what am I editing, if anything."
  const [formTarget, setFormTarget] = useState<{ id: number | null; name: string; description: string } | null>(
    null
  );
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    apiRequest<ProjectDTO[]>("/api/projects")
      .then(setProjects)
      .catch(() => toast.error("Couldn't load projects — check your connection and try again."));
    // eslint-disable-next-line react-hooks/exhaustive-deps -- fetch once on mount; toast identity isn't relevant here.
  }, []);

  const openCreateForm = () => setFormTarget({ id: null, name: "", description: "" });
  const openEditForm = (project: ProjectDTO) =>
    setFormTarget({ id: project.id, name: project.name, description: project.description });
  const closeForm = () => setFormTarget(null);

  const handleSubmitForm = async () => {
    if (!formTarget) return;
    // Required-field check only — UX, not the real gate; the backend re-validates with
    // @Valid on the create/update request DTO.
    if (!formTarget.name.trim()) {
      toast.error("Enter a project name.");
      return;
    }

    setSubmitting(true);
    try {
      const body = JSON.stringify({ name: formTarget.name, description: formTarget.description });
      if (formTarget.id === null) {
        const created = await apiRequest<ProjectDTO>("/api/projects", { method: "POST", body });
        setProjects((prev) => [...(prev ?? []), created]);
        toast.success("Project created");
      } else {
        const updated = await apiRequest<ProjectDTO>(`/api/projects/${formTarget.id}`, {
          method: "PATCH",
          body,
        });
        setProjects((prev) => prev?.map((p) => (p.id === updated.id ? updated : p)) ?? null);
        toast.success("Project updated");
      }
      closeForm();
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't save this project — check your connection and try again."
      );
    } finally {
      setSubmitting(false);
    }
  };

  const handleDelete = async (project: ProjectDTO) => {
    // Destructive-variant action goes through ConfirmDialog (frontend-design-system.md §2).
    // Plain-language description — "soft delete" and "isActive=false" are backend
    // vocabulary, not something to put in front of a manager deciding whether to click this.
    const confirmed = await confirm({
      title: `Delete ${project.name}?`,
      description:
        "This project will be hidden from new reports, but existing reports that reference it are unaffected.",
      variant: "destructive",
      confirmLabel: "Delete",
    });
    if (!confirmed) return;

    try {
      await apiRequest(`/api/projects/${project.id}`, { method: "DELETE" });
      // GET /api/projects only ever returns active projects (api-doc.md), so a deleted
      // project would vanish from a refetch anyway — removing it locally just matches that
      // without a second round trip.
      setProjects((prev) => prev?.filter((p) => p.id !== project.id) ?? null);
      toast.success("Project deleted");
    } catch (err) {
      toast.error(
        err instanceof ApiError ? err.message : "Couldn't delete this project — check your connection and try again."
      );
    }
  };

  const columns: DataTableColumn<ProjectDTO>[] = [
    { key: "name", header: "Name", render: (row) => row.name },
    { key: "description", header: "Description", render: (row) => row.description },
    {
      key: "actions",
      header: "",
      render: (row) => (
        <div className="flex justify-end gap-1">
          <Button variant="ghost" ariaLabel="Edit project" icon={<Pencil size={16} />} onClick={() => openEditForm(row)} />
          <Button
            variant="ghost"
            ariaLabel="Delete project"
            icon={<Trash2 size={16} />}
            onClick={() => handleDelete(row)}
          />
        </div>
      ),
    },
  ];

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-[28px] font-semibold text-ink">Projects</h1>
        <Button variant="primary" onClick={openCreateForm}>
          <Plus size={16} />
          Add project
        </Button>
      </div>

      {formTarget && (
        <div className="flex flex-col gap-3 rounded border border-line bg-surface p-4">
          <h2 className="text-sm font-semibold text-ink">
            {formTarget.id === null ? "New project" : "Edit project"}
          </h2>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Name
            <Input
              value={formTarget.name}
              onChange={(event) => setFormTarget({ ...formTarget, name: event.target.value })}
            />
          </label>
          <label className="flex flex-col gap-1 text-sm font-medium text-ink">
            Description
            <Textarea
              value={formTarget.description}
              onChange={(event) => setFormTarget({ ...formTarget, description: event.target.value })}
            />
          </label>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" onClick={closeForm} disabled={submitting}>
              Cancel
            </Button>
            <Button variant="primary" onClick={handleSubmitForm} disabled={submitting}>
              {submitting ? "Saving…" : "Save"}
            </Button>
          </div>
        </div>
      )}

      <DataTable columns={columns} rows={projects ?? []} rowKey={(row) => row.id} />
    </div>
  );
}
