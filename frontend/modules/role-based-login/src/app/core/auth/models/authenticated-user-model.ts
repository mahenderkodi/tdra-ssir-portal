export interface AuthenticatedUser {

  // Internal numeric user ID from backend.
  id: number;

  // Backend/user-facing unique user identifier.
  userIdString: string;

  username: string;
  email: string;

  // Roles used for route and permission checks.
  roles: string[];

  // Company linked to this user; null if no company is assigned yet.
  companyId: number | null;
}