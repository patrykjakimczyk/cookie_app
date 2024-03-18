import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';

import {
  AddUserToGroup,
  CreateGroupRequest,
  UpdateGroupRequest,
  UserWithAuthoritiesRequest,
} from 'src/app/shared/model/requests/groups-requests';
import {
  AssignAuthoritiesToUserResponse,
  GroupNameTakenResponse,
  GetUserGroupsResponse,
} from 'src/app/shared/model/responses/group-response';
import { GroupDetailsDTO } from 'src/app/shared/model/types/group-types';
import { environment } from 'src/environments/environment';

@Injectable({ providedIn: 'root' })
export class GroupService {
  private readonly url = environment.backendUrl;
  private readonly group_url = 'group';
  private readonly group_id_url = 'group/{id}';
  private readonly group_id_users_url = 'group/{id}/users';
  private readonly group_id_authorities_url = 'group/{id}/authorities';

  constructor(private http: HttpClient) {}

  createGroup(request: CreateGroupRequest) {
    return this.http.post<GroupNameTakenResponse>(
      this.url + this.group_url,
      request
    );
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
    return this.http.patch<GroupNameTakenResponse>(
      this.url + this.group_id_url.replace('{id}', groupId.toString()),
      body
    );
  }

  deleteGroup(groupId: number) {
    return this.http.delete<any>(
      this.url + this.group_id_url.replace('{id}', groupId.toString())
    );
  }

  addUserToGroup(groupId: number, addUser: AddUserToGroup) {
    return this.http.post<any>(
      this.url + this.group_id_users_url.replace('{id}', groupId.toString()),
      addUser
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

  assignAuthoritiesToUser(
    groupId: number,
    request: UserWithAuthoritiesRequest
  ) {
    return this.http.post<AssignAuthoritiesToUserResponse>(
      this.url +
        this.group_id_authorities_url.replace('{id}', groupId.toString()),
      request
    );
  }

  removeAuthoritiesFromUser(
    groupId: number,
    request: UserWithAuthoritiesRequest
  ) {
    return this.http.patch<any>(
      this.url +
        this.group_id_authorities_url.replace('{id}', groupId.toString()),
      request
    );
  }
}
