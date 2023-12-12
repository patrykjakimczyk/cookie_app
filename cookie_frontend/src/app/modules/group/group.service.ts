import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import {
  CreateGroupRequest,
  UpdateGroupRequest,
} from 'src/app/shared/model/requests/groups-requests';
import { GetUserGroupsResponse } from 'src/app/shared/model/responses/group-response';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly url = 'http://localhost:8081/';
  private readonly group_url = 'group';
  private readonly group_id_url = 'group/{id}';
  private readonly group_id_users_url = 'group/{id}/users';
  private readonly group_id_authorities_url = 'group/{id}/authorities';

  constructor(private http: HttpClient) {}

  createGroup(request: CreateGroupRequest) {
    return this.http.post<any>(this.url + this.group_url, request);
  }

  getUserGroups() {
    return this.http.get<GetUserGroupsResponse>(this.url + this.group_url);
  }

  getGroup(groupId: number) {
    return this.http.get<GroupDetailsDTO>(
      this.url + this.group_id_url.replace('{id}', groupId.toString())
    );
  }

  updateGroup(groupId: number, body: UpdateGroupRequest) {
    return this.http.patch<GroupDetailsDTO>(
      this.url + this.group_id_url.replace('{id}', groupId.toString()),
      body
    );
  }

  deleteGroup(groupId: number) {
    return this.http.delete<any>(
      this.url + this.group_id_url.replace('{id}', groupId.toString())
    );
  }

  removeUserFromGroup(groupId: number, userId: number) {
    let params = new HttpParams();
    params = params.append('userToRemoveId', userId);

    return this.http.delete<any>(
      this.url + this.group_id_users_url.replace('{id}', groupId.toString()),
      { params: params }
    );
  }
}
