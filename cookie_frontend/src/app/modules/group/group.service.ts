import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { GetUserGroupsResponse } from 'src/app/shared/model/responses/group-response';

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly url = 'http://localhost:8081/';
  private readonly group_url = 'group';
  private readonly group_id_url = 'group/{id}';
  private readonly group_id_users_url = 'group/{id}/users';
  private readonly group_id_authorities_url = 'group/{id}/authorities';

  constructor(private http: HttpClient) {}

  getUserGroups() {
    return this.http.get<GetUserGroupsResponse>(this.url + this.group_url);
  }
}
