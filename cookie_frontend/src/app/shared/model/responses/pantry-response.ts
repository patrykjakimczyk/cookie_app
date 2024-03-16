import { PantryDTO } from '../types/pantry-types';
import { AuthorityDTO } from '../types/user-types';

export type GetUserPantriesResponse = {
  pantries: PantryDTO[];
};

export type GetPantryResponse = {
  pantryId: number;
  pantryName: string;
  groupId: number;
  groupName: string;
  authorities: AuthorityDTO[];
};

export type DeletePantryResponse = {
  deletedPantryName: string;
};
