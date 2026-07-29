export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  username: string;
  roles: string[];
  companyId: number | null;
}