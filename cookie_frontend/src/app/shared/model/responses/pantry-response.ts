import { PantryDTO } from '../types/pantry-types';

export type GetUserPantriesResponse = {
  pantries: PantryDTO[];
};

export type GetPantryResponse = {
  id: number;
  pantryName: string;
};

export type DeletePantryResponse = {
  deletedPantryName: string;
};
