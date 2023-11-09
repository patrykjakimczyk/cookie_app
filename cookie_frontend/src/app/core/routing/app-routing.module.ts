import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from '../../modules/dashboard/dashboard.component';
import { RegistrationFormComponent } from '../../modules/registration-form/registration-form.component';
import { LoginFormComponent } from '../../modules/login-form/login-form.component';
import { PantryComponent } from 'src/app/modules/pantry/pantry.component';
import { CreatePantryComponent } from 'src/app/modules/pantry/create-pantry/create-pantry.component';
import { AuthGuard } from './auth-guard';

const routes: Routes = [
  {
    path: '',
    component: DashboardComponent,
  },
  {
    path: 'registration',
    component: RegistrationFormComponent,
  },
  {
    path: 'login',
    component: LoginFormComponent,
  },
  {
    path: 'pantry',
    canActivate: [AuthGuard],
    component: PantryComponent,
  },
  {
    path: 'pantry',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'create',
        component: CreatePantryComponent,
      },
    ],
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
  providers: [],
})
export class AppRoutingModule {}
