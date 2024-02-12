import { UserService } from 'src/app/shared/services/user-service';
import { GetShoppingListResponse } from './../../../shared/model/types/shopping-lists-types';
import { ShoppingListsService } from './../shopping-lists.service';
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { AuthorityEnum } from 'src/app/shared/model/enums/authority.enum';
import { MatDialog } from '@angular/material/dialog';
import { NewNamePopupComponentComponent } from 'src/app/shared/components/new-name-popup-component/new-name-popup-component.component';
import { RegexConstants } from 'src/app/shared/model/constants/regex-constants';
import { DeletePopupComponent } from 'src/app/shared/components/delete-popup/delete-popup.component';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';

@Component({
  selector: 'app-shopping-list',
  templateUrl: './shopping-list.component.html',
  styleUrls: ['./shopping-list.component.scss'],
})
export class ShoppingListComponent implements OnInit {
  protected shoppingList: GetShoppingListResponse = {
    id: 0,
    listName: '',
    authorities: [],
    assignedPantry: false,
  };
  protected shoppingList$ = new Subject<GetShoppingListResponse>();
  protected authorityEnum = AuthorityEnum;

  constructor(
    private shoppingListsService: ShoppingListsService,
    private route: ActivatedRoute,
    protected userService: UserService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    const listId = this.route.snapshot.params['id'];

    this.shoppingListsService.getShoppingList(listId).subscribe({
      next: (response: GetShoppingListResponse) => {
        this.userService.setUserAuthorities(response.authorities);
        this.shoppingList = response;
        this.shoppingList$.next(response);
      },
    });
  }

  openChangeListNameDialog() {
    const changePantryNameDialog = this.dialog.open(
      NewNamePopupComponentComponent,
      {
        data: {
          type: 'LIST',
          regex: RegexConstants.shoppingListRegex,
        },
      }
    );

    changePantryNameDialog.afterClosed().subscribe((newListName: string) => {
      this.shoppingListsService
        .updateShoppingList(this.shoppingList.id, {
          shoppingListName: newListName,
        })
        .subscribe({
          next: (response: GetShoppingListResponse) => {
            this.userService.setUserAuthorities(response.authorities);
            this.shoppingList = response;
          },
        });
    });
  }

  openDeletePantryDialog() {
    const deleteListDialog = this.dialog.open(DeletePopupComponent, {
      data: {
        header: 'Are you sure you want to delete this shopping list?',
        button: 'Delete shopping list',
      },
    });

    deleteListDialog.afterClosed().subscribe((deleteList) => {
      if (deleteList) {
        this.shoppingListsService
          .deleteShoppingList(this.shoppingList.id)
          .subscribe({
            next: (response) => {
              this.snackBar.open(
                `Pantry: ${response.deletedListName} has been deleted`,
                'Okay'
              );
            },
          });

        this.shoppingList = {
          id: 0,
          listName: '',
          authorities: [],
          assignedPantry: false,
        };
        this.shoppingList$.next(this.shoppingList);
      }
    });
  }
}
