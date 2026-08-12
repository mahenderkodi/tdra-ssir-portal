export interface SenderIdResponse {
  id: number;
  senderIdName: string;
  trackingId: string;
  status: string;
  createdAt: string;
  expirationDate: string | null;
  remarks: string | null;
  justification: string | null;
  companyName: string;
  authLetterUrl: string | null;
}