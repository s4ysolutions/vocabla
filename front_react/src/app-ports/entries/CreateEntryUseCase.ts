import type {Identifier} from '../../domain/identity/Identifier.ts';
import type {Tag} from '../../domain/Tag.ts';
import {Context, Effect} from 'effect';
import type {AppError} from '../errors/AppError.ts';
import type {Entry} from '../../domain/Entry.ts';

export type CreateEntryRequest = {
  readonly entry: Entry,
  readonly tagIds: Array<Identifier<Tag>>
}

export const CreateEntryRequest = (entry: Entry, tagIds: Array<Identifier<Tag>>): CreateEntryRequest => ({
  entry,
  tagIds
})

export type CreateEntryResponse = {
  readonly id: Identifier<Entry>;
}

export interface CreateEntryUseCase {
  createEntry(request: CreateEntryRequest): Effect.Effect<CreateEntryResponse, AppError>
}

export class CreateEntryUseCaseTag extends Context.Tag('CreateEntryUseCaseTag')<
  CreateEntryUseCaseTag,
  CreateEntryUseCase
>() {
}
