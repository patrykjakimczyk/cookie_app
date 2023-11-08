import { Component, Inject } from '@angular/core';
import { NgModel } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { PantryService } from '../pantry.service';

@Component({
  selector: 'app-change-pantry-name',
  templateUrl: './change-pantry-name.component.html',
  styleUrls: ['./change-pantry-name.component.scss'],
})
export class ChangePantryNameComponent {
  protected newPantryName: string = '';
  pantryNameRegex = RegexConstants.pantryNameRegex;

  constructor(
    public dialog: MatDialogRef<ChangePantryNameComponent>,
    private pantryService: PantryService
  ) {}

  close() {
    this.dialog.close();
  }

  changePantryName(newPantryName: NgModel) {
    if (!newPantryName.valid) {
      return;
    }

    this.pantryService
      .updateUserPantry({ pantryName: newPantryName.value })
      .subscribe({
        next: (response) => {
          this.dialog.close(response);
        },
      });
  }
}
