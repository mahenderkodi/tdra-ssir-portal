export interface AuthenticatedUser {
  id: number;
  userIdString: string;
  username: string;
  email: string;
  roles: string[];
  companyId: string | null;
}