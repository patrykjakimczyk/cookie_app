import { AuthorityEnum } from '../enums/authority.enum';

export type CreateGroupRequest = {
  groupName: string;
};

export type UpdateGroupRequest = {
  newGroupName: string;
};

export type AddUserToGroup = {
  usernameToAdd: string;
};

export type UserWithAuthoritiesRequest = {
  userId: number;
  authorities: AuthorityEnum[];
};
