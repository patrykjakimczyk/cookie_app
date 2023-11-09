import { Component } from '@angular/core';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { PantryService } from '../pantry.service';
import { NgModel } from '@angular/forms';
import { UserService } from 'src/app/shared/services/user-service';

@Component({
  selector: 'app-create-pantry',
  templateUrl: './create-pantry.component.html',
  styleUrls: ['./create-pantry.component.scss'],
})
export class CreatePantryComponent {
  protected pantryName = '';
  protected pantryNameRegex = RegexConstants.pantryNameRegex;
  protected createPantrySucceded = false;

  constructor(
    private pantryService: PantryService,
    private userService: UserService
  ) {}

  createPantry(name: NgModel) {
    if (!name.valid) {
      return;
    }

    this.pantryService.createUserPantry({ pantryName: name.value }).subscribe({
      next: (_) => {
        this.createPantrySucceded = true;
        this.userService.setUserAssignedPantry(true);
      },
    });
  }
}
