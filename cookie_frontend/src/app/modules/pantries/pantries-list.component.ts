import { Component, OnInit } from '@angular/core';
import { PantryService } from './pantry.service';
import { PantryDTO } from 'src/app/shared/model/types/pantry-types';

@Component({
  selector: 'app-pantries-list',
  templateUrl: './pantries-list.component.html',
  styleUrls: ['./pantries-list.component.scss'],
})
export class PantriesListComponent implements OnInit {
  protected pantries: PantryDTO[] = [];

  constructor(private pantryService: PantryService) {}

  ngOnInit(): void {
    // this.pantryService.getAllUserPantries().subscribe({
    //   next: (response) => {
    //     console.log(response);
    //     this.pantries = response.pantries;
    //   },
    // });
  }
}
