import { Component } from '@angular/core';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { ShoppingListsService } from '../../shopping-lists.service';
import { UserService } from 'src/app/shared/services/user-service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-create-shopping-list',
  templateUrl: './create-shopping-list.component.html',
  styleUrls: ['./create-shopping-list.component.scss'],
})
export class CreateShoppingListComponent {
  protected shoppingListRegex = RegexConstants.shoppingListRegex;
  protected createShoppingListSucceded = false;
  protected groupId = 0;
  protected createdShoppingListId = 0;

  constructor(
    private shoppingListsService: ShoppingListsService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    this.groupId = +this.route.snapshot.queryParamMap.get('groupId')!;
  }

  createShoppingList(name: string) {
    this.shoppingListsService
      .createShoppingList({ shoppingListName: name, groupId: this.groupId })
      .subscribe((response) => {
        this.createShoppingListSucceded = true;
        this.createdShoppingListId = response.id;
      });
  }
}
