import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login.component';
import { TaskListComponent } from './tasks/task-list.component';
import { TaskDetailComponent } from './tasks/task-detail.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'tasks', component: TaskListComponent },
  { path: 'tasks/:id', component: TaskDetailComponent },
];
