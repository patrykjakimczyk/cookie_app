import { GroupDetailsShoppingListDTO } from './../../../shared/model/types/group-types';
import { Component, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { FormBuilder, Validators } from '@angular/forms';

@Component({
  selector: 'app-add-to-list-popup',
  templateUrl: './add-to-list-popup.component.html',
  styleUrls: ['./add-to-list-popup.component.scss'],
})
export class AddToListPopupComponent {
  protected listForm = this.fb.group({
    listId: ['', Validators.required],
  });

  constructor(
    public dialog: MatDialogRef<AddToListPopupComponent>,
    @Inject(MAT_DIALOG_DATA)
    public shoppingLists: GroupDetailsShoppingListDTO[],
    private fb: FormBuilder
  ) {}

  close() {
    this.dialog.close(null);
  }

  add() {
    if (!this.listForm.valid) {
      return;
    }

    this.dialog.close(this.listForm.controls.listId.value);
  }
}
