import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  CreatePantryRequest,
  UpdatePantryRequest,
} from 'src/app/shared/model/requests/pantry-requests';
import {
  DeletePantryResponse,
  GetPantryResponse,
  GetUserPantriesResponse,
} from 'src/app/shared/model/responses/pantry-response';
import { PantryProductDTO } from './pantry-products-list/pantry-products-list.component';

@Injectable({ providedIn: 'root' })
export class PantryService {
  private readonly url = 'http://localhost:8081/';
  private readonly pantries_path = 'pantries';
  private readonly pantry_path = 'pantry';
  private readonly products_path = 'products';
  private readonly pantry_products_path = '/products';

  constructor(private http: HttpClient) {}

  getUserPantry(pantryId: number): Observable<GetPantryResponse> {
    return this.http.get<GetPantryResponse>(
      this.url + this.pantry_path + '/' + pantryId
    );
  }

  getAllUserPantries(): Observable<GetUserPantriesResponse> {
    return this.http.get<GetUserPantriesResponse>(
      this.url + this.pantries_path
    );
  }

  createUserPantry(
    request: CreatePantryRequest
  ): Observable<GetPantryResponse> {
    return this.http.post<GetPantryResponse>(
      this.url + this.pantry_path,
      request
    );
  }

  updateUserPantry(
    request: UpdatePantryRequest
  ): Observable<GetPantryResponse> {
    return this.http.patch<GetPantryResponse>(
      this.url + this.pantry_path,
      request
    );
  }

  deleteUserPantry(): Observable<DeletePantryResponse> {
    return this.http.delete<DeletePantryResponse>(this.url + this.pantry_path);
  }

  getPantryProducts(
    pantryId: number,
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
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}/${page}`,
      { params: params }
    );
  }

  addProductsToPantry(
    pantryId: number,
    pantryProducts: PantryProductDTO[]
  ): Observable<any> {
    return this.http.post<any>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      pantryProducts
    );
  }

  removeProductsFromPantry(
    pantryId: number,
    productsIds: number[]
  ): Observable<any> {
    return this.http.delete<void>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      { body: productsIds }
    );
  }

  modifyPantryProduct(
    pantryId: number,
    product: PantryProductDTO
  ): Observable<any> {
    return this.http.patch<void>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}`,
      product
    );
  }

  reservePantryProduct(
    pantryId: number,
    pantryProductId: number,
    reserved: number
  ): Observable<PantryProductDTO> {
    return this.http.patch<PantryProductDTO>(
      `${this.url}${this.pantry_path}/${pantryId}${this.pantry_products_path}/${pantryProductId}`,
      { reserved: reserved }
    );
  }

  getProductsWithFilter(filterValue: string): Observable<any> {
    let params = new HttpParams();

    params = params.append('filterValue', filterValue);

    return this.http.get<any>(`${this.url}${this.products_path}`, {
      params: params,
    });
  }
}
