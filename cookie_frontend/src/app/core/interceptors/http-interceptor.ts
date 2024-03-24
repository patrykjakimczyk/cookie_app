import {
  HttpEvent,
  HttpHandler,
  HttpHeaders,
  HttpInterceptor,
  HttpParams,
  HttpRequest,
} from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { getCookie } from 'typescript-cookie';

import { User } from 'src/app/shared/model/user';

@Injectable()
export class HttpRequestInterceptor implements HttpInterceptor {
  public readonly authorizationHeader = 'Authorization';
  public readonly basic = 'Basic ';
  public readonly bearer = 'Bearer ';

  constructor() {}

  intercept(
    req: HttpRequest<any>,
    next: HttpHandler
  ): Observable<HttpEvent<any>> {
    let httpHeaders = new HttpHeaders();
    let userJson;
    let xsrf;
    let jwt;
    let user: User;

    if ((jwt = window.sessionStorage.getItem('JwtToken'))) {
      httpHeaders = httpHeaders.append(this.authorizationHeader, jwt);
    } else if ((userJson = window.sessionStorage.getItem('user'))) {
      user = JSON.parse(userJson);

      if (user.email && user.password) {
        window.btoa(user.email + ':' + user.password);
        httpHeaders = httpHeaders.append(
          this.authorizationHeader,
          this.basic + window.btoa(user.email + ':' + user.password)
        );
      }
    }

    if ((xsrf = getCookie('XSRF-TOKEN')!)) {
      httpHeaders = httpHeaders.append('X-XSRF-TOKEN', xsrf);
    }

    httpHeaders = httpHeaders.append('X-Requested-With', 'XMLHttpRequest');
    const request = req.clone({
      headers: httpHeaders,
      withCredentials: true,
    });

    return next.handle(request);
  }
}
