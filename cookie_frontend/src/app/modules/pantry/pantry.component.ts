import { GetPantryResponse } from './../../shared/model/responses/pantry-response';
import { Component, OnInit } from '@angular/core';
import { PantryService } from './pantry.service';
import { MatDialog } from '@angular/material/dialog';
import { ChangePantryNameComponent } from './change-pantry-name/change-pantry-name.component';

@Component({
  selector: 'app-pantry',
  templateUrl: './pantry.component.html',
  styleUrls: ['./pantry.component.scss'],
})
export class PantryComponent implements OnInit {
  pantry: GetPantryResponse = { id: 0, pantryName: '' };

  constructor(private pantryService: PantryService, public dialog: MatDialog) {}

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
        this.pantryService
          .updateUserPantry({ pantryName: newPantryName })
          .subscribe({
            next: (response) => {
              this.pantry = response;
            },
          });
      }
    });
  }
}
