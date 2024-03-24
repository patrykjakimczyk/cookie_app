import {
  GetUserShoppingListsResponse,
  ShoppingListDTO,
} from './../../shared/model/types/shopping-lists-types';
import { Component, OnInit } from '@angular/core';
import { ShoppingListsService } from './shopping-lists.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-shopping-lists',
  templateUrl: './shopping-lists.component.html',
  styleUrls: ['./shopping-lists.component.scss'],
})
export class ShoppingListsComponent implements OnInit {
  protected userLists: Map<string, ShoppingListDTO[]> = new Map();
  protected userListsGroups: string[] = [];

  constructor(
    private shoppingListsService: ShoppingListsService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.shoppingListsService.getAllUserShoppingLists().subscribe({
      next: (response: GetUserShoppingListsResponse) => {
        for (const list of response.shoppingLists) {
          if (this.userLists.get(list.groupName)) {
            this.userLists.get(list.groupName)?.push(list);
          } else {
            this.userLists.set(list.groupName, [list]);
          }
        }
        this.userListsGroups = [...this.userLists.keys()];
      },
    });
  }

  goToGroup(groupId: number) {
    this.router.navigate(['/groups/' + groupId]);
  }

  goToShoppingList(shoppinglistId: number) {
    this.router.navigate(['/shopping-lists/' + shoppinglistId]);
  }
}
