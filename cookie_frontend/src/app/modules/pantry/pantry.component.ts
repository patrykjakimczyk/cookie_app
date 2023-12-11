import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { GetPantryResponse } from './../../shared/model/responses/pantry-response';
import { Component, OnInit } from '@angular/core';
import { PantryService } from './pantry.service';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { UserService } from 'src/app/shared/services/user-service';
import { Subject } from 'rxjs';
import { NewNamePopupComponentComponent } from 'src/app/shared/components/new-name-popup-component/new-name-popup-component.component';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';

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
    const changePantryNameDialog = this.dialog.open(
      NewNamePopupComponentComponent,
      { data: { type: 'PANTRY', regex: RegexConstants.pantryNameRegex } }
    );

    changePantryNameDialog.afterClosed().subscribe((newPantryName: string) => {
      this.pantryService
        .updateUserPantry({ pantryName: newPantryName })
        .subscribe({
          next: (response: GetPantryResponse) => {
            this.pantry.pantryName = response.pantryName;
          },
        });
    });
  }

  openDeletePantryDialog() {
    const deletePantryDialog = this.dialog.open(DeletePopupComponent, {
      data: 'PANTRY',
    });

    deletePantryDialog.afterClosed().subscribe((deletePantry) => {
      if (deletePantry) {
        this.pantryService.deleteUserPantry().subscribe({
          next: (response) => {
            this.snackBar.open(
              `Pantry: ${response.deletedPantryName} has been deleted`,
              'Okay'
            );
          },
        });

        this.userService.setUserAssignedPantry(false);
        this.pantry = { id: 0, pantryName: '' };
        this.pantry$.next(this.pantry);
      }
    });
  }
}
