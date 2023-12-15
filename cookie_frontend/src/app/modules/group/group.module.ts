import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';

import { CreateGroupComponent } from './create-group/create-group.component';
import { GroupDetailsComponent } from './group-details/group-details.component';
import { SharedModule } from 'src/app/shared/shared.module';
import { GroupComponent } from './group.component';
import { AddUserComponent } from './group-details/add-user/add-user.component';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';

@NgModule({
  declarations: [
    GroupComponent,
    CreateGroupComponent,
    GroupDetailsComponent,
    AddUserComponent,
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,

    SharedModule,

    MatButtonModule,
    MatCardModule,
    MatListModule,
    MatDialogModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatCheckboxModule,
  ],
})
export class GroupModule {}
