// Mirrors ProjectDTO from GET /api/projects (docs/api/api-doc.md) — only the fields the
// report form's project dropdown actually needs.
export interface ProjectDTO {
  id: number;
  name: string;
  description: string;
  isActive: boolean;
}
