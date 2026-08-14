export interface LoginResponse {

  // Short-lived JWT used to access protected backend APIs.
  accessToken: string;

  // Used to obtain a new access token when the current one expires.
  refreshToken: string;

  // Usually "Bearer", indicating how the access token is sent.
  tokenType: string;

  username: string;

  // User roles used for authorization and route access.
  roles: string[];

  // Company linked to the logged-in user.
  companyId: number | null;

  // Indicates whether the user must complete first-time password setup.
  firstTimeLogin?: boolean;
}