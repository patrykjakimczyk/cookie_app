import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';

import { NewNamePopupComponentComponent } from 'src/app/shared/components/new-name-popup-component/new-name-popup-component.component';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { UserService } from 'src/app/shared/services/user-service';
import { PantriesService } from '../pantries.service';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { GetPantryResponse } from '../../../shared/model/responses/pantry-response';
import { ActivatedRoute, Router } from '@angular/router';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority.enum';

@Component({
  selector: 'app-pantry',
  templateUrl: './pantry.component.html',
  styleUrls: ['./pantry.component.scss'],
})
export class PantryComponent implements OnInit {
  protected pantry: GetPantryResponse = {
    pantryId: 0,
    pantryName: '',
    groupId: 0,
    groupName: '',
    authorities: [],
  };
  protected pantry$ = new Subject<GetPantryResponse>();
  protected authorityEnum = AuthorityEnum;

  constructor(
    private pantryService: PantriesService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    protected userService: UserService,
    private route: ActivatedRoute,
    private router: Router
  ) {}

  ngOnInit(): void {
    const pantryId = this.route.snapshot.params['id'];

    this.pantryService.getUserPantry(pantryId).subscribe({
      next: (response) => {
        this.userService.setUserAuthorities(response.authorities);
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
        .updateUserPantry(this.pantry.pantryId, { pantryName: newPantryName })
        .subscribe({
          next: (response: GetPantryResponse) => {
            this.pantry.pantryName = response.pantryName;
          },
        });
    });
  }

  openDeletePantryDialog() {
    const deletePantryDialog = this.dialog.open(DeletePopupComponent, {
      data: {
        header: 'Are you sure you want to delete this pantry?',
        button: 'Delete pantry',
      },
    });

    deletePantryDialog.afterClosed().subscribe((deletePantry) => {
      if (deletePantry) {
        this.pantryService.deleteUserPantry(this.pantry.pantryId).subscribe({
          next: (response) => {
            this.snackBar.open(
              `Pantry: ${response.deletedPantryName} has been deleted`,
              'Okay'
            );
            this.pantry = {
              pantryId: 0,
              pantryName: '',
              groupId: 0,
              groupName: '',
              authorities: [],
            };
            this.pantry$.next(this.pantry);
          },
        });
      }
    });
  }
}
