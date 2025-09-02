export interface Claim {
  id?: number;
  disasterType?: string;
  description?: string;
  requestAmount?: number;
  status?: string;
  incidentDate?: string;
  location?: string;
  
  user?: {
    id: number;
    username: string;
    fullName: string;
    email: string;
  };

  reviewerId?: number;
  reviewComments?: string;
  approvedAmount?: number;
  
  createdAt?: string;
  updatedAt?: string;
  reviewedAt?: string;
}