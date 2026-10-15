import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH';

export interface Task {
  id: number;
  title: string;
  description: string;
  completed: boolean;
  ownerId: number;
  createdAt: string;
  priority: TaskPriority;
}

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly baseUrl = 'http://localhost:8080/api/tasks';

  constructor(private http: HttpClient) {}

  list(): Observable<Task[]> {
    return this.http.get<Task[]>(this.baseUrl);
  }

  get(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.baseUrl}/${id}`);
  }

  create(title: string, description: string, priority?: TaskPriority): Observable<Task> {
    return this.http.post<Task>(this.baseUrl, { title, description, priority });
  }

  update(id: number, title: string, description: string, priority?: TaskPriority): Observable<Task> {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, { title, description, priority });
  }

  complete(id: number): Observable<Task> {
    return this.http.patch<Task>(`${this.baseUrl}/${id}/complete`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
