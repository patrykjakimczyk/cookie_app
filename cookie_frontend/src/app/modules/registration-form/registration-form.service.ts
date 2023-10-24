import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { RegistrationResponse } from 'src/app/shared/model/responses/registration-response';
import { map } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class RegistrationFormService {
  private readonly url = 'http://localhost:8081/';
  private readonly register_path = 'register';

  constructor(private http: HttpClient) {}

  register(body: any) {
    return this.http
      .post<RegistrationResponse>(this.url + this.register_path, body)
      .pipe(map((response) => response.duplicates));
  }
}
