import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Task, TaskService } from './task.service';

describe('TaskService', () => {
  let service: TaskService;
  let httpMock: HttpTestingController;

  const task: Task = {
    id: 5,
    title: 'Write report',
    description: 'Quarterly numbers',
    completed: false,
    ownerId: 1,
    createdAt: '2026-10-13T09:30:00',
    priority: 'MEDIUM',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TaskService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => httpMock.verify());

  it('get() requests a single task by id', () => {
    let result: Task | undefined;

    service.get(5).subscribe((t) => (result = t));

    const req = httpMock.expectOne('http://localhost:8080/api/tasks/5');
    expect(req.request.method).toBe('GET');
    req.flush(task);
    expect(result).toEqual(task);
  });

  it('get() propagates a 404 error', () => {
    let status: number | undefined;

    service.get(99).subscribe({ error: (e) => (status = e.status) });

    httpMock
      .expectOne('http://localhost:8080/api/tasks/99')
      .flush({ error: 'Task not found: 99' }, { status: 404, statusText: 'Not Found' });
    expect(status).toBe(404);
  });

  it('create() sends priority in the request body', () => {
    service.create('t', 'd', 'HIGH').subscribe();

    const req = httpMock.expectOne('http://localhost:8080/api/tasks');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ title: 't', description: 'd', priority: 'HIGH' });
    req.flush({ ...task, priority: 'HIGH' });
  });

  it('update() PUTs title, description and priority', () => {
    let result: Task | undefined;

    service.update(5, 't', 'd', 'LOW').subscribe((t) => (result = t));

    const req = httpMock.expectOne('http://localhost:8080/api/tasks/5');
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({ title: 't', description: 'd', priority: 'LOW' });
    req.flush({ ...task, priority: 'LOW' });
    expect(result?.priority).toBe('LOW');
  });
});
