import { PantryDTO } from '../types/pantry-types';
import { AuthorityDTO } from '../types/user-types';

export type GetUserPantriesResponse = {
  pantries: PantryDTO[];
};

export type GetPantryResponse = {
  id: number;
  pantryName: string;
  authorities: AuthorityDTO[];
};

export type DeletePantryResponse = {
  deletedPantryName: string;
};
