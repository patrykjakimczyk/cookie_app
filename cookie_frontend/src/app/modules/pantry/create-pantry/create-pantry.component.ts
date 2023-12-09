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
  protected pantryNameRegex = RegexConstants.pantryNameRegex;
  protected createPantrySucceded = false;

  constructor(
    private pantryService: PantryService,
    private userService: UserService
  ) {}

  createPantry(name: string) {
    this.pantryService.createUserPantry({ pantryName: name }).subscribe({
      next: (_) => {
        this.createPantrySucceded = true;
        this.userService.setUserAssignedPantry(true);
      },
    });
  }
}
