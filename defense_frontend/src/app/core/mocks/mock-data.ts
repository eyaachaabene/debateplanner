import { Role, Major, DefenseStatus, Mention, JuryRole } from '../models';
import { Student } from '../models/student.model';
import { Professor } from '../models/professor.model';
import { Room } from '../models/room.model';
import { Defense} from '../models/defense.model';

// Helper to generate dates
const today = new Date();
const formatDate = (date: Date): string => date.toISOString().split('T')[0];
const addDays = (date: Date, days: number): Date => {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
};

// Mock-only types for fake auth/demo data
export enum MockRole {
  ADMIN = 'ADMIN',
  PROFESSOR = 'PROFESSOR',
  STUDENT = 'STUDENT'
}

export interface MockUser {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: MockRole;
  password: string;
}

// ========== MOCK USERS ==========
export const MOCK_USERS: (MockUser & { password: string })[] = [
  { id: 1, email: 'admin@university.fr', firstName: 'Jean', lastName: 'Dupont', role: MockRole.ADMIN, password: 'admin123' },
  { id: 2, email: 'prof.martin@university.fr', firstName: 'Marie', lastName: 'Martin', role: MockRole.PROFESSOR, password: 'prof123' },
  { id: 3, email: 'prof.bernard@university.fr', firstName: 'Pierre', lastName: 'Bernard', role: MockRole.PROFESSOR, password: 'prof123' },
  { id: 4, email: 'prof.petit@university.fr', firstName: 'Sophie', lastName: 'Petit', role: MockRole.PROFESSOR, password: 'prof123' },
  { id: 5, email: 'prof.durand@university.fr', firstName: 'Luc', lastName: 'Durand', role: MockRole.PROFESSOR, password: 'prof123' },
  { id: 6, email: 'etudiant.leroy@university.fr', firstName: 'Thomas', lastName: 'Leroy', role: MockRole.STUDENT, password: 'student123' },
  { id: 7, email: 'etudiant.moreau@university.fr', firstName: 'Emma', lastName: 'Moreau', role: MockRole.STUDENT, password: 'student123' },
];

// ========== MOCK PROFESSORS ==========
export const MOCK_PROFESSORS: Professor[] = [
  {
    id: 1,
    userId: 2,
    firstName: 'Marie',
    lastName: 'Martin',
    email: 'marie.martin@university.fr'
  },
  {
    id: 2,
    userId: 3,
    firstName: 'Pierre',
    lastName: 'Bernard',
    email: 'pierre.bernard@university.fr'
  },
  {
    id: 3,
    firstName: 'Sophie',
    lastName: 'Petit',
    email: 'sophie.petit@university.fr'
  },
  {
    id: 4,
    firstName: 'Luc',
    lastName: 'Durand',
    email: 'luc.durand@university.fr'
  },
  {
    id: 5,
    firstName: 'Claire',
    lastName: 'Fontaine',
    email: 'claire.fontaine@university.fr'
  },
  {
    id: 6,
    firstName: 'Francois',
    lastName: 'Girard',
    email: 'francois.girard@university.fr'
  }
];

// ========== MOCK STUDENTS ==========
export const MOCK_STUDENTS: Student[] = [
  {
    id: 1,
    userId: 6,
    firstName: 'Thomas',
    lastName: 'Leroy',
    email: 'thomas.leroy@university.fr',
    major: Major.ENGINEERING_COMPUTER_SCIENCE,
    level: 5
  },
  {
    id: 2,
    userId: 7,
    firstName: 'Emma',
    lastName: 'Moreau',
    email: 'emma.moreau@university.fr',
    major: Major.MASTER_SOFTWARE_ENGINEERING,
    level: 5
  },
  {
    id: 3,
    firstName: 'Lucas',
    lastName: 'Garcia',
    email: 'lucas.garcia@university.fr',
    major: Major.LICENCE_BIG_DATA,
    level: 5
  },
  {
    id: 4,
    firstName: 'Chloe',
    lastName: 'Dubois',
    email: 'chloe.dubois@university.fr',
    major: Major.MASTER_ARTIFICIAL_INTELLIGENCE,
    level: 5
  },
  {
    id: 5,
    firstName: 'Antoine',
    lastName: 'Roux',
    email: 'antoine.roux@university.fr',
    major: Major.LICENCE_MULTIMEDIA,
    level: 5
  },
  {
    id: 6,
    firstName: 'Lea',
    lastName: 'Simon',
    email: 'lea.simon@university.fr',
    major: Major.DOCTORATE_COMPUTER_SCIENCE,
    level: 5
  },
  {
    id: 7,
    firstName: 'Hugo',
    lastName: 'Laurent',
    email: 'hugo.laurent@university.fr',
    major: Major.ENGINEERING_COMPUTER_SCIENCE,
    level: 5
  },
  {
    id: 8,
    firstName: 'Camille',
    lastName: 'Morel',
    email: 'camille.morel@university.fr',
    major: Major.MASTER_SOFTWARE_ENGINEERING,
    level: 5
  }
];
// ========== MOCK ROOMS ==========
export const MOCK_ROOMS: Room[] = [
  {
    id: 1,
    name: 'Amphitheatre Turing',
    capacity: 150,
  },
  {
    id: 2,
    name: 'Salle Einstein',
    capacity: 30
  },
  {
    id: 3,
    name: 'Salle Curie',
    capacity: 25,
  },
  {
    id: 4,
    name: 'Salle Newton',
    capacity: 20
  },
  {
    id: 5,
    name: 'Salle Darwin',
    capacity: 35
  },
  {
    id: 6,
    name: 'Salle Lavoisier',
    capacity: 25,
  }
]

// ========== MOCK DEFENSES ==========
export const MOCK_DEFENSES: Defense[] = [
  {
    id: 1,
    projectTitle: 'Système de détection de fraudes bancaires',
    defenseDate: formatDate(addDays(today, 7)),
    startTime: '14:00',
    endTime: '16:00',
    status: DefenseStatus.PLANNED,
    studentId: 1,
    supervisorId: 1,
    presidentId: 2,
    reviewerId: 5,
    examinerId: 6,
    roomId: 2
  },
  {
    id: 2,
    projectTitle: 'Systèmes embarqués pour véhicules autonomes',
    defenseDate: formatDate(addDays(today, 14)),
    startTime: '10:00',
    endTime: '12:00',
    status: DefenseStatus.PLANNED,
    studentId: 2,
    supervisorId: 2,
    presidentId: 1,
    reviewerId: 3,
    examinerId: 4,
    roomId: 4
  },
  {
    id: 3,
    projectTitle: 'Blockchain et sécurité des systèmes distribués',
    defenseDate: formatDate(addDays(today, -5)),
    startTime: '09:00',
    endTime: '11:00',
    status: DefenseStatus.PUBLISHED,
    finalAverage: 17.5,
    mention: Mention.VERY_GOOD,
    presidentGrade: 17,
    reviewerGrade: 18,
    examinerGrade: 17.5,
    supervisorGrade: 17.5,
    studentId: 4,
    supervisorId: 1,
    presidentId: 5,
    reviewerId: 2,
    examinerId: 6,
    roomId: 1
  },
  {
    id: 4,
    projectTitle: 'Convertisseurs de puissance pour énergies renouvelables',
    defenseDate: formatDate(addDays(today, -10)),
    startTime: '14:00',
    endTime: '16:00',
    status: DefenseStatus.PUBLISHED,
    finalAverage: 15,
    mention: Mention.PASSABLE,
    presidentGrade: 15,
    reviewerGrade: 15.5,
    examinerGrade: 14.5,
    supervisorGrade: 15,
    studentId: 6,
    supervisorId: 6,
    presidentId: 3,
    reviewerId: 5,
    examinerId: 4,
    roomId: 5
  },
  {
    id: 5,
    projectTitle: 'Traitement automatique du langage naturel',
    defenseDate: formatDate(addDays(today, 3)),
    startTime: '10:00',
    endTime: '12:00',
    status: DefenseStatus.PLANNED,
    studentId: 8,
    supervisorId: 1,
    presidentId: 2,
    reviewerId: 5,
    examinerId: 6,
    roomId: 2
  },
  {
    id: 6,
    projectTitle: 'Simulation numérique des écoulements turbulents',
    defenseDate: formatDate(addDays(today, 21)),
    startTime: '14:00',
    endTime: '16:00',
    status: DefenseStatus.PLANNED,
    studentId: 3,
    supervisorId: 3,
    presidentId: 4,
    reviewerId: 5,
    examinerId: 2,
    roomId: 3
  }
];

// ========== MOCK RESULTS (from completed defenses) ==========
export interface MockDefenseResult {
  defenseId: number;
  studentId: number;
  projectTitle: string;
  defenseDate: string;
  finalAverage: number;
  mention: string;
}

export const MOCK_RESULTS: MockDefenseResult[] = MOCK_DEFENSES
  .filter(d => d.status === DefenseStatus.PUBLISHED && d.finalAverage !== undefined)
  .map(d => ({
    defenseId: d.id,
    studentId: d.studentId,
    projectTitle: d.projectTitle,
    defenseDate: d.defenseDate,
    finalAverage: d.finalAverage!,
    mention: d.mention || ''
  }));

// ========== AUTH CREDENTIALS MAPPING ==========
type MockAuthUser = Omit<MockUser, 'password'>;

export const MOCK_AUTH_CREDENTIALS: Record<string, { password: string; user: MockAuthUser }> = {};

MOCK_USERS.forEach(u => {
  MOCK_AUTH_CREDENTIALS[u.email] = {
    password: u.password,
    user: {
      id: u.id,
      email: u.email,
      firstName: u.firstName,
      lastName: u.lastName,
      role: u.role
    }
  };
});

// Available time slots for scheduling
export const AVAILABLE_TIME_SLOTS = [
  '08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
  '11:00', '11:30', '14:00', '14:30', '15:00', '15:30',
  '16:00', '16:30', '17:00', '17:30'
];

