import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@environments/environment';
import { Room, RoomDto } from '../models';
import { PagedResponse } from '../models/paged-response.model';

export interface RoomFilters {
  search?: string;
  page?: number;
  size?: number;
}

@Injectable({
  providedIn: 'root'
})
export class RoomService {
  private readonly apiUrl = `${environment.apiUrl}/rooms`;

  constructor(private http: HttpClient) {}

  getAll(filters?: RoomFilters): Observable<Room[]> {
    let params = new HttpParams();

    if (filters?.search) {
      params = params.set('search', filters.search);
    }

    if (filters?.page !== undefined) {
      params = params.set('page', filters.page.toString());
    }

    if (filters?.size !== undefined) {
      params = params.set('size', filters.size.toString());
    }

    return this.http.get<Room[]>(this.apiUrl, { params });
  }

  getById(id: number): Observable<Room> {
    return this.http.get<Room>(`${this.apiUrl}/${id}`);
  }

  create(room: RoomDto): Observable<Room> {
    return this.http.post<Room>(this.apiUrl, room);
  }

  update(id: number, room: RoomDto): Observable<Room> {
    return this.http.put<Room>(`${this.apiUrl}/${id}`, room);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  getAvailable(date: string, startTime: string, endTime: string, excludeDefenseId?: number): Observable<Room[]> {
    let params = new HttpParams()
      .set('date', date)
      .set('startTime', startTime)
      .set('endTime', endTime);

    if (excludeDefenseId !== undefined) {
      params = params.set('excludeDefenseId', excludeDefenseId.toString());
    }

    return this.http.get<Room[]>(`${this.apiUrl}/available`, { params });
  }
}