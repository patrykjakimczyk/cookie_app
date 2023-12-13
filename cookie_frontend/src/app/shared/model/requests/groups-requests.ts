export type CreateGroupRequest = {
  groupName: string;
};

export type UpdateGroupRequest = {
  newGroupName: string;
};

export type AddUserToGroup = {
  usernameToAdd: string;
};
