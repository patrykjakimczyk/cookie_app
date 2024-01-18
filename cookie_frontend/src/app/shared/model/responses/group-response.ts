import { GroupDTO } from '../types/group-types';
import { AuthorityDTO } from '../types/user-types';

export type GroupNameTakenResponse = {
  groupNameTaken: boolean;
};

export type GetUserGroupsResponse = {
  userGroups: GroupDTO[];
};

export type AssignAuthoritiesToUserResponse = {
  assignedAuthorities: AuthorityDTO[];
};
