import {Schema} from 'effect';
import type {components} from '../../../rest/types.ts';
import {identifierFromNumberStudent} from '../studend/identifier.ts';
import type {Entry} from '../../../../domain/Entry.ts';

type EntryDTO = components['schemas']['Entry']

export const entryFromDto: Schema.Schema<Entry, EntryDTO> =
  Schema.Struct({ //dto

    ownerId: identifierFromNumberStudent
  })
