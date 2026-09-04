import { UserRole } from "./authContext";

// Mirrors UserDTO from GET /api/users (docs/api/api-doc.md) — passwordHash is never present
// in any response shape, so it's not modeled here either.
export interface UserDTO {
  id: number;
  name: string;
  email: string;
  role: UserRole;
  isActive: boolean;
}
