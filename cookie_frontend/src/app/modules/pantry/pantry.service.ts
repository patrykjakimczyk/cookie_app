import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';
import { UpdatePantryRequest } from 'src/app/shared/model/requests/pantry-requests';
import {
  DeletePantryResponse,
  GetPantryResponse,
} from 'src/app/shared/model/responses/pantry-response';

@Injectable({ providedIn: 'root' })
export class PantryService {
  private readonly url = 'http://localhost:8081/';
  private readonly pantry_path = 'pantry';

  constructor(private http: HttpClient) {}

  getUserPantry(): Observable<GetPantryResponse> {
    return this.http.get<GetPantryResponse>(this.url + this.pantry_path);
  }

  createUserPantry(request: UpdatePantryRequest): Observable<any> {
    return this.http.post<any>(this.url + this.pantry_path, request);
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
}
