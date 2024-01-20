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
import { MatExpansionModule } from '@angular/material/expansion';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSelectModule } from '@angular/material/select';
import { ReactiveFormsModule } from '@angular/forms';
import { MatIconModule } from '@angular/material/icon';

@NgModule({
  declarations: [GroupComponent, CreateGroupComponent, GroupDetailsComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,
    ReactiveFormsModule,

    SharedModule,

    MatButtonModule,
    MatCardModule,
    MatListModule,
    MatDialogModule,
    MatSnackBarModule,
    MatExpansionModule,
    MatCheckboxModule,
    MatSelectModule,
    MatIconModule,
  ],
})
export class GroupModule {}
