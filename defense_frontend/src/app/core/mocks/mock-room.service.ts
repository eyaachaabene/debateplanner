import { Injectable } from '@angular/core';
import { Observable, of, delay, throwError } from 'rxjs';
import { Room, RoomDto } from '../models';
import { MOCK_ROOMS, MOCK_DEFENSES } from './mock-data';
import { PagedResponse } from '../models/paged-response.model';

export interface RoomFilters {
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class MockRoomService {
  private rooms: Room[] = [...MOCK_ROOMS];
  private nextId = this.rooms.length + 1;

  getAll(filters?: RoomFilters): Observable<PagedResponse<Room>> {
    let filtered = [...this.rooms];

    if (filters?.search) {
      const search = filters.search.toLowerCase();
      filtered = filtered.filter(r =>
        r.name.toLowerCase().includes(search)
      );
    }

    const page = filters?.page ?? 0;
    const size = filters?.size ?? 10;
    const start = page * size;
    const end = start + size;
    const paged = filtered.slice(start, end);

    const response: PagedResponse<Room> = {
      content: paged,
      totalElements: filtered.length,
      totalPages: Math.ceil(filtered.length / size),
      size,
      number: page
    };

    return of(response).pipe(delay(300));
  }

  getById(id: number): Observable<Room> {
    const room = this.rooms.find(r => r.id === id);
    if (room) {
      return of(room).pipe(delay(200));
    }
    return throwError(() => new Error('Salle non trouvée')).pipe(delay(200));
  }

  create(request: RoomDto): Observable<Room> {
    const newRoom: Room = {
      id: this.nextId++,
      name: request.name,
      capacity: request.capacity
    };

    this.rooms.push(newRoom);
    return of(newRoom).pipe(delay(400));
  }

  update(id: number, request: RoomDto): Observable<Room> {
    const index = this.rooms.findIndex(r => r.id === id);

    if (index !== -1) {
      this.rooms[index] = {
        id,
        name: request.name,
        capacity: request.capacity
      };

      return of(this.rooms[index]).pipe(delay(400));
    }

    return throwError(() => new Error('Salle non trouvée')).pipe(delay(200));
  }

  delete(id: number): Observable<void> {
    const index = this.rooms.findIndex(r => r.id === id);

    if (index !== -1) {
      this.rooms.splice(index, 1);
      return of(void 0).pipe(delay(300));
    }

    return throwError(() => new Error('Salle non trouvée')).pipe(delay(200));
  }

  getAvailable(date: string, startTime: string, endTime: string, excludeDefenseId?: number): Observable<Room[]> {
    const busyRoomIds = new Set(
      MOCK_DEFENSES
        .filter(d =>
          d.defenseDate === date &&
          (excludeDefenseId === undefined || d.id !== excludeDefenseId) &&
          this.timesOverlap(d.startTime, d.endTime, startTime, endTime)
        )
        .map(d => d.roomId)
    );

    const availableRooms = this.rooms.filter(room => !busyRoomIds.has(room.id));
    return of(availableRooms).pipe(delay(300));
  }

  private timesOverlap(start1: string, end1: string, start2: string, end2: string): boolean {
    return start1 < end2 && end1 > start2;
  }
}