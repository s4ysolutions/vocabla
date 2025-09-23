import {Context, Effect} from 'effect';
import {type Identifier} from '../../domain/identity/Identifier.ts';
import type {Student} from '../../domain/Student.ts';
import {type EntriesFilter} from '../../domain/EntriesFilter.ts';
import {type Entry} from '../../domain/Entry.ts';
import type {AppError} from '../errors/AppError.ts';
import type {Identified} from '../../domain/identity/Identified.ts';


export type GetEntriesByOwnerRequest = {
  readonly ownerId: Identifier<Student>;
  readonly filter: EntriesFilter
}

/*
export const schemaGetEntriesByOwnerRequest = Schema.Struct({
  ownerId: schemaIdentifier<Student>(),
  filter: schemaEntriesFilter,
})
*/
export type GetEntriesByOwnerResponse = {
  readonly entries: ReadonlyArray<Identified<Entry>>;
}

/*
export const schemaGetEntriesByOwnerResponse = Schema.Struct({
  entries: Schema.Array(schemaIdentified<Entry>(schemaEntry))
});

export type GetEntriesByOwnerResponse = typeof schemaGetEntriesByOwnerResponse.Type;
*/

export interface GetEntriesByOwnerUseCase {
  getEntriesByOwner(request: GetEntriesByOwnerRequest): Effect.Effect<GetEntriesByOwnerResponse, AppError>;
}

export class GetEntriesByOwnerUseCaseTag extends Context.Tag('GetEntriesByOwnerUseCaseTag')<
  GetEntriesByOwnerUseCaseTag,
  GetEntriesByOwnerUseCase
>() {
}
