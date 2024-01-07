import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CreateShoppingListRequest,
  DeleteShoppingListResponse,
  GetShoppingListResponse,
  GetUserShoppingListsResponse,
  UpdateShoppingListRequest,
} from 'src/app/shared/model/types/shopping-lists-types';

@Injectable({ providedIn: 'root' })
export class ShoppingListsService {
  private readonly url = 'http://localhost:8081/';
  private readonly shopping_lists_path = 'shopping-lists';
  private readonly shopping_list_path = 'shopping-list';
  private readonly shopping_list_id_path = 'shopping-list/{id}';
  private readonly list_products_page_path = '/products/{page}';

  constructor(private http: HttpClient) {}

  createShoppingList(
    createListBody: CreateShoppingListRequest
  ): Observable<GetShoppingListResponse> {
    return this.http.post<GetShoppingListResponse>(
      this.url + this.shopping_list_path,
      createListBody
    );
  }

  getAllUserShoppingLists(): Observable<GetUserShoppingListsResponse> {
    return this.http.get<GetUserShoppingListsResponse>(
      this.url + this.shopping_lists_path
    );
  }

  getShoppingList(listId: number): Observable<GetShoppingListResponse> {
    return this.http.get<GetShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString())
    );
  }

  deleteShoppingList(listId: number): Observable<DeleteShoppingListResponse> {
    return this.http.delete<DeleteShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString())
    );
  }

  updateShoppingList(
    listId: number,
    updateListBody: UpdateShoppingListRequest
  ): Observable<GetShoppingListResponse> {
    return this.http.patch<GetShoppingListResponse>(
      this.url + this.shopping_list_id_path.replace('{id}', listId.toString()),
      updateListBody
    );
  }

  getShoppingListsProducts(
    listId: number,
    page: number,
    filterValue: string,
    sortColName: string,
    sortDirection: string
  ): Observable<any> {
    let params = new HttpParams();

    params = params
      .append('filterValue', filterValue)
      .append('sortColName', sortColName)
      .append('sortDirection', sortDirection);

    return this.http.get<any>(
      `${this.url}${this.shopping_list_id_path.replace(
        '{id}',
        listId.toString()
      )}${this.list_products_page_path.replace('{page}', page.toString())}`,
      { params: params }
    );
  }
}
