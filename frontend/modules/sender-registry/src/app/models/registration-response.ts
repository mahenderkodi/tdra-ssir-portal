export interface RegistrationSubmissionResponseModel {
  trackingId: string;
  status: string;
  message: string;
  username: string;
  tempPassword: string;
  submittedAt: string;
}