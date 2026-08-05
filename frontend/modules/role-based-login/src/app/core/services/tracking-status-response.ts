export interface TrackingStatusResponse {
  trackingId: string;
  companyName: string;
  currentStatus:
    | 'SUBMITTED'
    | 'UNDER_REVIEW'
    | 'INFO_REQUESTED'
    | 'APPROVED'
    | 'REJECTED';
  submittedAt: string;
}