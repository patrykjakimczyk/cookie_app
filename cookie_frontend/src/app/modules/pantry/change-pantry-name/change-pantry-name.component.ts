import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-change-pantry-name',
  templateUrl: './change-pantry-name.component.html',
  styleUrls: ['./change-pantry-name.component.scss'],
})
export class ChangePantryNameComponent {
  protected newPantryName: string = '';

  constructor(public dialog: MatDialogRef<ChangePantryNameComponent>) {}

  close() {
    this.dialog.close();
  }

  changePantryName() {
    this.dialog.close(this.newPantryName);
  }
}
