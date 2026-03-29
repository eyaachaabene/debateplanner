export interface Professor {
  id: number;
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
}

export interface ProfessorRequest {
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
}