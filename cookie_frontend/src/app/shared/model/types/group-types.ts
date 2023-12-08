import { UserDTO } from './user-types';

export type GroupDTO = {
  id: number;
  groupName: string;
  creator: UserDTO;
  users: number;
};

export type GroupDetailsDTO = {
  id: number;
  groupName: string;
  creator: UserDTO;
  users: UserDTO[];
};
