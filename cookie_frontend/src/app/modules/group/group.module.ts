import { SharedModule } from 'src/app/shared/shared.module';
import { GroupComponent } from './group.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { CreateGroupComponent } from './create-group/create-group.component';
import { RouterModule } from '@angular/router';
import { GroupDetailsComponent } from './group-details/group-details.component';
import { MatCardModule } from '@angular/material/card';
import { MatListModule } from '@angular/material/list';

@NgModule({
  declarations: [GroupComponent, CreateGroupComponent, GroupDetailsComponent],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    RouterModule,

    SharedModule,

    MatButtonModule,
    MatCardModule,
    MatListModule,
  ],
})
export class GroupModule {}
