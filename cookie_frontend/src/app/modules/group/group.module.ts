import { SharedModule } from 'src/app/shared/shared.module';
import { GroupComponent } from './group.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

@NgModule({
  declarations: [GroupComponent],
  imports: [BrowserModule, BrowserAnimationsModule, SharedModule],
  exports: [GroupComponent],
})
export class GroupModule {}
