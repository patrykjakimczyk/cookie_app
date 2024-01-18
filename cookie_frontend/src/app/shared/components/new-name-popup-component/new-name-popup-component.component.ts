import { Component, Inject } from '@angular/core';
import { NgModel } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { NewNameType } from './new-name.type';

export type NewNameData = {
  type: NewNameType;
  regex: string;
  nameTaken: boolean;
};

@Component({
  selector: 'app-new-name-popup-component',
  templateUrl: './new-name-popup-component.component.html',
  styleUrls: ['./new-name-popup-component.component.scss'],
})
export class NewNamePopupComponentComponent {
  protected newName: string = '';

  constructor(
    public dialog: MatDialogRef<NewNamePopupComponentComponent>,
    @Inject(MAT_DIALOG_DATA) public data: NewNameData
  ) {}

  close() {
    this.dialog.close();
  }

  changeName(newName: NgModel) {
    if (!newName.valid) {
      return;
    }

    this.dialog.close(newName.value);
  }
}
