import { Component, OnInit } from '@angular/core';
import { PantriesService } from './pantries.service';
import { PantryDTO } from 'src/app/shared/model/types/pantry-types';
import { Router } from '@angular/router';

@Component({
  selector: 'app-pantries-list',
  templateUrl: './pantries-list.component.html',
  styleUrls: ['./pantries-list.component.scss'],
})
export class PantriesListComponent implements OnInit {
  protected pantries: PantryDTO[] = [];

  constructor(private pantryService: PantriesService, private router: Router) {}

  ngOnInit(): void {
    this.pantryService.getAllUserPantries().subscribe((response) => {
      this.pantries = response.pantries;
    });
  }

  goToGroup(groupId: number) {
    this.router.navigate(['/groups/' + groupId]);
  }

  goToPantry(pantryId: number) {
    this.router.navigate(['/pantries/' + pantryId]);
  }
}
