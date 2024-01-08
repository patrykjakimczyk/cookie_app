import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { DeleteTexts } from './delete-texts-type';

@Component({
  selector: 'app-delete-popup',
  templateUrl: './delete-popup.component.html',
  styleUrls: ['./delete-popup.component.scss'],
})
export class DeletePopupComponent {
  constructor(
    public dialog: MatDialogRef<DeletePopupComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DeleteTexts
  ) {}

  close() {
    this.dialog.close(false);
  }

  delete() {
    this.dialog.close(true);
  }
}
