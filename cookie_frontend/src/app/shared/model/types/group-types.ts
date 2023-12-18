import { UserDTO } from './user-types';

export type GroupDTO = {
  id: number;
  groupName: string;
  creator: UserDTO;
  users: number;
  pantryId: number;
};

export type GroupDetailsDTO = {
  id: number;
  groupName: string;
  creator: UserDTO;
  users: UserDTO[];
  pantryId: number;
  pantryName: string;
};
