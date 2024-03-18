import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { map } from 'rxjs';

import { RegistrationResponse } from 'src/app/shared/model/responses/registration-responses';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class RegistrationFormService {
  private readonly url = environment.backendUrl;
  private readonly register_path = 'register';

  constructor(private http: HttpClient) {}

  register(body: any) {
    return this.http
      .post<RegistrationResponse>(this.url + this.register_path, body)
      .pipe(map((response) => response.duplicates));
  }
}
