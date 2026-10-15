import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Task, TaskPriority, TaskService } from './task.service';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  template: `
    <div class="task-detail">
      <a routerLink="/tasks">&larr; Back to tasks</a>

      <p *ngIf="loading">Loading...</p>
      <p *ngIf="error" class="error">{{ error }}</p>

      <div *ngIf="task" class="details">
        <h2>{{ task.title }}</h2>
        <p class="status" [class.completed]="task.completed">
          {{ task.completed ? 'Completed' : 'Open' }}
        </p>
        <dl>
          <dt>Description</dt>
          <dd>{{ task.description || 'No description' }}</dd>
          <dt>Priority</dt>
          <dd>
            <select [ngModel]="task.priority" (ngModelChange)="changePriority($event)" aria-label="Priority">
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
            </select>
          </dd>
          <dt>Created</dt>
          <dd>{{ task.createdAt | date: 'medium' }}</dd>
        </dl>
      </div>
    </div>
  `,
  styles: [
    `
      .task-detail { max-width: 480px; margin: 2rem auto; }
      .details { margin-top: 1rem; }
      .status { font-weight: 600; }
      .status.completed { color: #16a34a; }
      dt { font-weight: 600; margin-top: 0.75rem; }
      dd { margin: 0.25rem 0 0; }
      .error { color: #dc2626; }
    `,
  ],
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  loading = false;
  error = '';

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
  ) {}

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    if (!Number.isInteger(id) || id <= 0) {
      this.error = 'Invalid task id';
      return;
    }

    this.loading = true;
    this.taskService.get(id).subscribe({
      next: (task) => {
        this.task = task;
        this.loading = false;
      },
      error: () => {
        this.error = 'Task not found';
        this.loading = false;
      },
    });
  }

  changePriority(priority: TaskPriority): void {
    if (!this.task) {
      return;
    }
    const { id, title, description } = this.task;
    this.taskService.update(id, title, description, priority).subscribe({
      next: (updated) => (this.task = updated),
      error: () => (this.error = 'Could not update priority'),
    });
  }
}
