import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ActivatedRoute, convertToParamMap, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { TaskDetailComponent } from './task-detail.component';
import { Task, TaskService } from './task.service';

describe('TaskDetailComponent', () => {
  let fixture: ComponentFixture<TaskDetailComponent>;
  let taskService: jasmine.SpyObj<TaskService>;

  const task: Task = {
    id: 5,
    title: 'Write report',
    description: 'Quarterly numbers',
    completed: false,
    ownerId: 1,
    createdAt: '2026-10-13T09:30:00',
    priority: 'MEDIUM',
  };

  function setup(id: string | null): void {
    TestBed.configureTestingModule({
      imports: [TaskDetailComponent],
      providers: [
        provideRouter([]),
        { provide: TaskService, useValue: taskService },
        {
          provide: ActivatedRoute,
          useValue: { snapshot: { paramMap: convertToParamMap(id === null ? {} : { id }) } },
        },
      ],
    });
    fixture = TestBed.createComponent(TaskDetailComponent);
    fixture.detectChanges();
  }

  function text(): string {
    return (fixture.nativeElement as HTMLElement).textContent ?? '';
  }

  beforeEach(() => {
    taskService = jasmine.createSpyObj<TaskService>('TaskService', ['get', 'update']);
  });

  it('loads the task for the id in the route and shows its details', () => {
    taskService.get.and.returnValue(of(task));

    setup('5');

    expect(taskService.get).toHaveBeenCalledWith(5);
    expect(text()).toContain('Write report');
    expect(text()).toContain('Quarterly numbers');
    expect(text()).toContain('Open');
  });

  it('shows Completed status for a completed task', () => {
    taskService.get.and.returnValue(of({ ...task, completed: true }));

    setup('5');

    expect(text()).toContain('Completed');
  });

  it('shows a placeholder when the task has no description', () => {
    taskService.get.and.returnValue(of({ ...task, description: '' }));

    setup('5');

    expect(text()).toContain('No description');
  });

  it('shows an error when the task is not found', () => {
    taskService.get.and.returnValue(throwError(() => new Error('404')));

    setup('99');

    expect(text()).toContain('Task not found');
    expect(fixture.componentInstance.task).toBeNull();
    expect(fixture.componentInstance.loading).toBeFalse();
  });

  it('shows an error and skips the request for a non-numeric id', () => {
    setup('abc');

    expect(taskService.get).not.toHaveBeenCalled();
    expect(text()).toContain('Invalid task id');
  });

  it('shows an error and skips the request when the id is missing', () => {
    setup(null);

    expect(taskService.get).not.toHaveBeenCalled();
    expect(text()).toContain('Invalid task id');
  });

  it('shows an error and skips the request for a non-positive id', () => {
    setup('0');

    expect(taskService.get).not.toHaveBeenCalled();
    expect(text()).toContain('Invalid task id');
  });

  it('links back to the task list', () => {
    taskService.get.and.returnValue(of(task));

    setup('5');

    const link = (fixture.nativeElement as HTMLElement).querySelector('a');
    expect(link?.getAttribute('href')).toBe('/tasks');
  });

  it('shows the task priority', () => {
    taskService.get.and.returnValue(of({ ...task, priority: 'HIGH' }));

    setup('5');

    const select = (fixture.nativeElement as HTMLElement).querySelector('select') as HTMLSelectElement;
    fixture.whenStable().then(() => expect(select.value).toBe('HIGH'));
    expect(fixture.componentInstance.task?.priority).toBe('HIGH');
  });

  it('updates via TaskService.update with title, description and the new priority', () => {
    taskService.get.and.returnValue(of(task));
    taskService.update.and.returnValue(of({ ...task, priority: 'LOW' }));
    setup('5');

    fixture.componentInstance.changePriority('LOW');

    expect(taskService.update).toHaveBeenCalledWith(5, 'Write report', 'Quarterly numbers', 'LOW');
    expect(fixture.componentInstance.task?.priority).toBe('LOW');
  });

  it('shows an error when the priority update fails', () => {
    taskService.get.and.returnValue(of(task));
    taskService.update.and.returnValue(throwError(() => new Error('500')));
    setup('5');

    fixture.componentInstance.changePriority('HIGH');
    fixture.detectChanges();

    expect(text()).toContain('Could not update priority');
  });
});
