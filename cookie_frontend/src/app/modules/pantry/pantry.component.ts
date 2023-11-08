import { GetPantryResponse } from './../../shared/model/responses/pantry-response';
import { Component, OnInit } from '@angular/core';
import { PantryService } from './pantry.service';
import { MatDialog } from '@angular/material/dialog';
import { ChangePantryNameComponent } from './change-pantry-name/change-pantry-name.component';
import { DeletePantryComponent } from './delete-pantry/delete-pantry.component';
import { MatSnackBar } from '@angular/material/snack-bar';

@Component({
  selector: 'app-pantry',
  templateUrl: './pantry.component.html',
  styleUrls: ['./pantry.component.scss'],
})
export class PantryComponent implements OnInit {
  protected pantry: GetPantryResponse = { id: 0, pantryName: '' };

  constructor(
    private pantryService: PantryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.pantryService.getUserPantry().subscribe({
      next: (response) => {
        this.pantry = response;
      },
    });
  }

  openChangePantryNameDialog() {
    const changePantryNameDialog = this.dialog.open(ChangePantryNameComponent);

    changePantryNameDialog.afterClosed().subscribe((newPantryName) => {
      if (newPantryName) {
        this.pantry = newPantryName;
      }
    });
  }

  openDeletePantryDialog() {
    const deletePantryDialog = this.dialog.open(DeletePantryComponent);

    deletePantryDialog.afterClosed().subscribe((deletedPantryName) => {
      if (deletedPantryName) {
        this.snackBar.open(
          `Pantry: ${deletedPantryName} has been deleted`,
          'Okay'
        );

        this.pantry = { id: 0, pantryName: '' };
      }
    });
  }
}
