import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { TaskListComponent } from './task-list.component';
import { Task, TaskService } from './task.service';

describe('TaskListComponent', () => {
  let fixture: ComponentFixture<TaskListComponent>;
  let taskService: jasmine.SpyObj<TaskService>;

  const tasks: Task[] = [
    { id: 1, title: 'First', description: '', completed: false, ownerId: 1, createdAt: '2026-10-13T09:00:00', priority: 'HIGH' },
    { id: 2, title: 'Second', description: 'd', completed: true, ownerId: 1, createdAt: '2026-10-13T10:00:00', priority: 'LOW' },
  ];

  beforeEach(() => {
    taskService = jasmine.createSpyObj<TaskService>('TaskService', ['list', 'create', 'complete', 'delete']);
    taskService.list.and.returnValue(of(tasks));

    TestBed.configureTestingModule({
      imports: [TaskListComponent],
      providers: [provideRouter([]), { provide: TaskService, useValue: taskService }],
    });
    fixture = TestBed.createComponent(TaskListComponent);
    fixture.detectChanges();
  });

  it('links each task title to its detail page', () => {
    const links = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('li a'));

    expect(links.map((a) => a.getAttribute('href'))).toEqual(['/tasks/1', '/tasks/2']);
    expect(links.map((a) => a.textContent?.trim())).toEqual(['First', 'Second']);
  });

  it('does not create a task when the title is blank', () => {
    fixture.componentInstance.newTitle = '   ';

    fixture.componentInstance.addTask();

    expect(taskService.create).not.toHaveBeenCalled();
  });

  it('defaults the create priority to MEDIUM and passes the selection to the service', () => {
    const cmp = fixture.componentInstance;
    taskService.create.and.returnValue(of(tasks[0]));
    expect(cmp.newPriority).toBe('MEDIUM');

    cmp.newTitle = 'New';
    cmp.newDescription = 'desc';
    cmp.newPriority = 'HIGH';
    cmp.addTask();

    expect(taskService.create).toHaveBeenCalledWith('New', 'desc', 'HIGH');
    expect(cmp.newPriority).toBe('MEDIUM');
  });

  it('shows the priority label on each task', () => {
    const labels = Array.from((fixture.nativeElement as HTMLElement).querySelectorAll('li .priority'));

    expect(labels.map((l) => l.textContent?.trim())).toEqual(['HIGH', 'LOW']);
  });
});
