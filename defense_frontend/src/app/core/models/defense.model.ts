import { DefenseStatus, Mention } from './enums';

export interface Defense {
  id: number;
  projectTitle: string;
  defenseDate: string;
  startTime: string;
  endTime: string;
  status: DefenseStatus;
  finalAverage?: number;
  mention?: Mention;
  presidentGrade?: number;
  reviewerGrade?: number;
  examinerGrade?: number;
  supervisorGrade?: number;
  studentId: number;
  supervisorId: number;
  presidentId: number;
  reviewerId: number;
  examinerId: number;
  roomId: number;
}

export interface DefenseRequest {
  projectTitle: string;
  defenseDate: string;
  startTime: string;
  endTime: string;
  studentId: number;
  supervisorId: number;
  presidentId: number;
  reviewerId: number;
  examinerId: number;
  roomId: number;
}

export interface CheckConflictsRequest {
  defenseDate: string;
  startTime: string;
  endTime: string;
  studentId: number;
  supervisorId: number;
  presidentId: number;
  reviewerId: number;
  examinerId: number;
  roomId: number;
  excludeDefenseId?: number;
}

export interface JuryAssignmentRequest {
  supervisorId: number;
  presidentId: number;
  reviewerId: number;
  examinerId: number;
}

export interface DefenseGrades {
  presidentGrade?: number;
  reviewerGrade?: number;
  examinerGrade?: number;
  supervisorGrade?: number;
  finalAverage?: number;
  mention?: Mention;
}

export interface ReportMetadata {
  id: number;
  defenseId: number;
  originalFilename: string;
  contentType: string;
  fileSize: number;
  uploadedAt: string;
  uploadedBy: string;
}