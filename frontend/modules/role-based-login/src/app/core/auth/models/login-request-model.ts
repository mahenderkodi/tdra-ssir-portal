export interface LoginRequest {

  // User can log in using either username or email.
  usernameOrEmail: string;

  password: string;
}