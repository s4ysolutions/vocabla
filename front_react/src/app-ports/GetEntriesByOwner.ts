import {Context, Effect, Schema} from 'effect';
import {schemaIdentifier} from '../domain/identity/Identifier.ts';
import type {Student} from '../domain/Student.ts';
import {schemaEntriesFilter} from '../domain/EntriesFilter.ts';
import {schemaIdentified} from '../domain/identity/Identified.ts';
import {type Entry, schemaEntry} from '../domain/Entry.ts';
import type {AppError} from './errors/AppError.ts';

export const schemaGetEntriesByOwnerRequest = Schema.Struct({
  ownerId: schemaIdentifier<Student>(),
  filter: schemaEntriesFilter,
})

export type GetEntriesByOwnerRequest = typeof schemaGetEntriesByOwnerRequest.Type;

export const schemaGetEntriesByOwnerResponse = Schema.Struct({
  entries: Schema.Array(schemaIdentified<Entry>(schemaEntry))
});

export type GetEntriesByOwnerResponse = typeof schemaGetEntriesByOwnerResponse.Type;

export interface GetEntriesByOwnerUseCase {
  getEntriesByOwner(request: GetEntriesByOwnerRequest): Effect.Effect<GetEntriesByOwnerResponse, AppError>;
}

export class GetEntriesByOwnerUseCaseTag extends Context.Tag('GetEntriesByOwnerUseCaseTag')<
  GetEntriesByOwnerUseCaseTag,
  GetEntriesByOwnerUseCase
>() {
}
