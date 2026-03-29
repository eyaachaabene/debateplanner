import { Provider } from '@angular/core';
import { environment } from '@environments/environment';

// Real Services
import { AuthService } from '../services/auth.service';
import { StudentService } from '../services/student.service';
import { ProfessorService } from '../services/professor.service';
import { RoomService } from '../services/room.service';
import { DefenseService } from '../services/defense.service';

// Mock Services
import { MockAuthService } from '../mocks/mock-auth.service';
import { MockStudentService } from '../mocks/mock-student.service';
import { MockProfessorService } from '../mocks/mock-professor.service';
import { MockRoomService } from '../mocks/mock-room.service';
import { MockDefenseService } from '../mocks/mock-defense.service';

// Service tokens for dependency injection
export const AUTH_SERVICE = 'AuthService';
export const STUDENT_SERVICE = 'StudentService';
export const PROFESSOR_SERVICE = 'ProfessorService';
export const ROOM_SERVICE = 'RoomService';
export const DEFENSE_SERVICE = 'DefenseService';

export function provideServices(): Provider[] {
  if (environment.useMocks) {
    return [
      { provide: AUTH_SERVICE, useClass: MockAuthService },
      { provide: STUDENT_SERVICE, useClass: MockStudentService },
      { provide: PROFESSOR_SERVICE, useClass: MockProfessorService },
      { provide: ROOM_SERVICE, useClass: MockRoomService },
      { provide: DEFENSE_SERVICE, useClass: MockDefenseService },
      // Also provide as their own classes for direct injection
      { provide: AuthService, useClass: MockAuthService },
      { provide: StudentService, useClass: MockStudentService },
      { provide: ProfessorService, useClass: MockProfessorService },
      { provide: RoomService, useClass: MockRoomService },
      { provide: DefenseService, useClass: MockDefenseService },
    ];
  }
  
  return [
    { provide: AUTH_SERVICE, useClass: AuthService },
    { provide: STUDENT_SERVICE, useClass: StudentService },
    { provide: PROFESSOR_SERVICE, useClass: ProfessorService },
    { provide: ROOM_SERVICE, useClass: RoomService },
    { provide: DEFENSE_SERVICE, useClass: DefenseService },
    AuthService,
    StudentService,
    ProfessorService,
    RoomService,
    DefenseService,
  ];
}
