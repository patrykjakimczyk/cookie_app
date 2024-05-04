import { GroupDTO } from '../types/group-types';
import { AuthorityDTO } from '../types/user-types';

export type GetGroupResponse = {
  groupId: number;
};

export type GetUserGroupsResponse = {
  userGroups: GroupDTO[];
};

export type AssignAuthoritiesToUserResponse = {
  assignedAuthorities: AuthorityDTO[];
};
