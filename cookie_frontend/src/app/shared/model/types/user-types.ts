import { AuthorityEnum } from '../enums/authority.enum';

export type AuthorityDTO = {
  authority: AuthorityEnum;
  userId: number;
  groupId: number;
};

export type UserDTO = {
  id: number;
  username: string;
  authorities: AuthorityDTO[];
};
