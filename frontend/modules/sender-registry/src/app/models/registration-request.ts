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

export interface RegistrationRequest {
  company: CompanyDto;
  documents?: any;
  representative?: any;
  account?: any;
}