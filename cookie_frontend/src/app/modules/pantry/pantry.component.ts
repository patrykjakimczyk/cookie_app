import { GetPantryResponse } from './../../shared/model/responses/pantry-response';
import { Component, OnInit } from '@angular/core';
import { PantryService } from './pantry.service';
import { MatDialog } from '@angular/material/dialog';
import { ChangePantryNameComponent } from './change-pantry-name/change-pantry-name.component';
import { DeletePantryComponent } from './delete-pantry/delete-pantry.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from 'src/app/shared/services/user-service';
import { Subject, config } from 'rxjs';

@Component({
  selector: 'app-pantry',
  templateUrl: './pantry.component.html',
  styleUrls: ['./pantry.component.scss'],
})
export class PantryComponent implements OnInit {
  protected pantry: GetPantryResponse = { id: 0, pantryName: '' };
  protected pantry$ = new Subject<GetPantryResponse>();

  constructor(
    private pantryService: PantryService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.pantryService.getUserPantry().subscribe({
      next: (response) => {
        this.pantry = response;
        this.pantry$.next(response);
      },
    });
  }

  openChangePantryNameDialog() {
    const changePantryNameDialog = this.dialog.open(ChangePantryNameComponent);

    changePantryNameDialog
      .afterClosed()
      .subscribe((newPantry: GetPantryResponse) => {
        if (newPantry) {
          this.pantry = newPantry;
          this.pantry$.next(newPantry);
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

        this.userService.setUserAssignedPantry(false);
        this.pantry = { id: 0, pantryName: '' };
        this.pantry$.next(this.pantry);
      }
    });
  }
}
