export interface CompanyDto {
  companyName: string;
  legalEntityName: string;
  tradeLicenseNumber: string;
  registrationNumber: string;
  taxId: string;
  companyType: string;
  industry: string;
  dateOfIncorporation: string;
  registeredAddress: string;
  country: string;
  emirateState: string;
  city: string;
  postalCode: string;
  website?: string;
  companyEmail: string;
  companyPhone: string;
}


export interface RepresentativeDto {
  firstName: string;
  lastName: string;
  designation: string;
  department: string;
  officialEmail: string;
  mobileNumber: string;
  officeNumber: string;
  address: string;
  uaePassId: string;
  passportOrEmiratesId: string;
}


export interface AccountDto {
  username: string;
  preferredLanguage: string;
  timeZone: string;
  mfaPreference: string;
  notificationPreference: string;
}


/*
 * Documents are uploaded separately as multipart File objects.
 *
 * Therefore, the JSON registrationData currently sends
 * an empty documents object.
 */
export interface DocumentsDto {
}


export interface RegistrationRequest {
  company: CompanyDto;
  documents: DocumentsDto;
  representative: RepresentativeDto;
  account: AccountDto;
}


export interface RegistrationSubmissionResponseModel {
  trackingId: string;
  status: string;
  message: string;
  submittedAt: string;
}