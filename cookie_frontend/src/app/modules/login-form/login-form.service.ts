import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';

import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class LoginFormService {
  private readonly url = 'http://localhost:8081/';
  private readonly register_path = 'login';

  constructor(private http: HttpClient) {}

  login(): Observable<any> {
    return this.http.get<any>(this.url + this.register_path, {
      observe: 'response',
      withCredentials: true,
    });
  }

  test() {
    this.http
      .get<any>(this.url + 'test', {
        observe: 'response',
        withCredentials: true,
      })
      .subscribe((response) => {
        console.log(response);
      });
  }
}
