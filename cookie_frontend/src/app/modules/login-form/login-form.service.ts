import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class LoginFormService {
  private readonly url = environment.backendUrl;
  private readonly login_path = 'user';

  constructor(private http: HttpClient) {}

  login(): Observable<any> {
    return this.http.get<any>(this.url + this.login_path, {
      observe: 'response',
    });
  }
}
