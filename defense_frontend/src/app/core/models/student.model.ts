import { Major } from './enums';

export interface Student {
  id: number;
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
  major: Major;
  level: number;
}

export interface StudentRequest {
  userId?: number;
  firstName: string;
  lastName: string;
  email: string;
  major: Major;
  level: number;
}