import { PantriesService } from '../../pantries.service';
import { Component } from '@angular/core';
import { MatDialogRef } from '@angular/material/dialog';

@Component({
  selector: 'app-delete-pantry',
  templateUrl: './delete-pantry.component.html',
  styleUrls: ['./delete-pantry.component.scss'],
})
export class DeletePantryComponent {
  constructor(
    public dialog: MatDialogRef<DeletePantryComponent>,
    private pantryService: PantriesService
  ) {}

  close() {
    this.dialog.close();
  }

  deletePantry() {
    this.pantryService.deleteUserPantry().subscribe({
      next: (response) => {
        this.dialog.close(response.deletedPantryName);
      },
    });
  }
}
