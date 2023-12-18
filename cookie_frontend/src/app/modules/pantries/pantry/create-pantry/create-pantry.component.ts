import { Component, OnInit } from '@angular/core';

import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { PantryService } from '../../pantry.service';
import { UserService } from 'src/app/shared/services/user-service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-create-pantry',
  templateUrl: './create-pantry.component.html',
  styleUrls: ['./create-pantry.component.scss'],
})
export class CreatePantryComponent implements OnInit {
  protected pantryNameRegex = RegexConstants.pantryNameRegex;
  protected createPantrySucceded = false;
  protected groupId = 0;
  protected createdPantryId = 0;

  constructor(
    private pantryService: PantryService,
    private userService: UserService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.groupId = +this.route.snapshot.queryParamMap.get('groupId')!;
    console.log(this.groupId);
  }

  createPantry(name: string) {
    this.pantryService
      .createUserPantry({ pantryName: name, groupId: this.groupId })
      .subscribe({
        next: (response) => {
          this.createPantrySucceded = true;
          this.createdPantryId = response.id;
        },
      });
  }
}
