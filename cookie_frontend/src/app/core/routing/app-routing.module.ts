import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from '../../modules/dashboard/dashboard.component';
import { RegistrationFormComponent } from '../../modules/registration-form/registration-form.component';
import { LoginFormComponent } from '../../modules/login-form/login-form.component';
import { AuthGuard } from './auth-guard';
import { GroupComponent } from 'src/app/modules/group/group.component';
import { CreateGroupComponent } from 'src/app/modules/group/create-group/create-group.component';
import { GroupDetailsComponent } from 'src/app/modules/group/group-details/group-details.component';
import { PantriesListComponent } from 'src/app/modules/pantries/pantries-list.component';
import { CreatePantryComponent } from 'src/app/modules/pantries/pantry/create-pantry/create-pantry.component';
import { PantryComponent } from 'src/app/modules/pantries/pantry/pantry.component';
import { ShoppingListsComponent } from 'src/app/modules/shopping-lists/shopping-lists.component';
import { ShoppingListComponent } from 'src/app/modules/shopping-lists/shopping-list/shopping-list.component';
import { CreateShoppingListComponent } from 'src/app/modules/shopping-lists/shopping-list/create-shopping-list/create-shopping-list.component';

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
    path: 'pantries',
    canActivate: [AuthGuard],
    component: PantriesListComponent,
  },
  {
    path: 'pantries',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'create',
        component: CreatePantryComponent,
      },
      {
        path: ':id',
        component: PantryComponent,
      },
    ],
  },
  {
    path: 'groups',
    canActivate: [AuthGuard],
    component: GroupComponent,
  },
  {
    path: 'groups',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'create',
        component: CreateGroupComponent,
      },
      {
        path: ':id',
        component: GroupDetailsComponent,
      },
    ],
  },
  {
    path: 'shopping-lists',
    canActivate: [AuthGuard],
    component: ShoppingListsComponent,
  },
  {
    path: 'shopping-lists',
    canActivate: [AuthGuard],
    children: [
      {
        path: 'create',
        component: CreateShoppingListComponent,
      },
      {
        path: ':id',
        component: ShoppingListComponent,
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
