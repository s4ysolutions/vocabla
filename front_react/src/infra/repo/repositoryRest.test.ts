import {describe, expect, it} from '@effect/vitest';
import {Layer, Option} from 'effect';
import httpClientLayer from '../http/httpClientLive.ts';
import {Effect} from 'effect';
import {TagsRepositoryTag} from '../../app-repo/TagsRepository.ts';
import {restClientLayer} from '../rest/restClientLayer.ts';
import {repositoryRestLayer} from './repositoryRestLayer.ts';
import {decodeGetTagResponse, type GetTagResponse} from './dto/tag/GetTagResponse.ts';
import {type CreateTagResponse, decodeCreateTagResponse} from './dto/tag/CreateTagResponse.ts';
import {Identifier} from '../../domain/identity/Identifier.ts';
import {Tag} from '../../domain/Tag.ts';
import {Definition, Entry} from '../../domain/Entry.ts';
import {Localized} from '../../domain/Localized.ts';
import {LangCode} from '../../domain/LangCode.ts';
import type {Student} from '../../domain/Student.ts';
import {EntriesRepositoryTag} from '../../app-repo/EntriesRepository.ts';

describe('repositoryRest', () => {
  const layer: Layer.Layer<TagsRepositoryTag | EntriesRepositoryTag> = repositoryRestLayer.pipe(
    Layer.provide(restClientLayer),
    Layer.provide(httpClientLayer)
  )
  describe('schemas', () => {
    describe('tags', () => {
      it('schemaCreateTagResponse', () => {
        const response: CreateTagResponse = {tagId: 123};
        const tageId = Effect.runSync(decodeCreateTagResponse(response));
        expect(tageId).toEqual(Identifier(123));
      });
      it('schemaCreateTagResponse error', () => {
        const response: CreateTagResponse = {tagId: 'abc'} as unknown as CreateTagResponse;
        const decode = decodeCreateTagResponse(response);
        expect(() => Effect.runSync(decode)).toThrowError();
      });
      it('schemaGetTagResponse data', () => {
        const response: GetTagResponse = {tag: {label: 'test', ownerId: 1}};
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isSome(tag)).toBeTruthy();
        if (Option.isSome(tag)) {
          expect(tag.value).toEqual(Tag('test', Identifier(1)));
        }
        if (Option.isNone(tag)) {
          throw new Error('Expected Some, got None');
        }
      });
      it('schemaGetTagResponse null', () => {
        const response: GetTagResponse = {tag: null}
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isNone(tag)).toBeTruthy();
      })
      it('schemaGetTagResponse absent', () => {
        const response: GetTagResponse = {tagx: {label: 'test', ownerId: 1}} as unknown as GetTagResponse;
        const tag = Effect.runSync(decodeGetTagResponse(response));
        expect(Option.isNone(tag)).toBeTruthy();
      })
    })
  });
  describe('integration tests', () => {
    describe('tags', () => {
      it.effect('createTag', () => {
        const program = Effect.gen(function* () {
          const tagsRepository = yield* TagsRepositoryTag;
          const tagId = yield* tagsRepository.createTag(Tag('test', Identifier(1)));
          console.log('Created tag with id:', tagId);
          expect(tagId).toBeDefined();
        });
        return Effect.provide(program, layer);
      })
      it.effect('getTag', () => {
        const program = Effect.gen(function* () {
          const tagsRepository = yield* TagsRepositoryTag;
          const label = 'test-' + Math.floor(Math.random() * 10000);
          const tagId = yield* tagsRepository.createTag(Tag(label, Identifier(1)));
          const tag = yield* tagsRepository.getTag(tagId);
          expect(tag._tag).toBe('Some');
          if (tag._tag === 'Some') {
            expect(tag.value.label).toBe(label);
            expect(tag.value.ownerId).toEqual(Identifier(1));
          }
        });
        return Effect.provide(program, layer);
      })
    })
    describe('entries', () => {
      it.effect('createEntry', () => {
        const program = Effect.gen(function* () {
          const entryRepository = yield* EntriesRepositoryTag;
          const entryId = yield* entryRepository.createEntry(Entry(Localized(LangCode('en'), 'dog'), [
            Definition(Localized(LangCode('en'), 'A domesticated carnivorous mammal')),
            Definition(Localized(LangCode('fr'), 'Un mammifère carnivore domestiqué'))
          ], Identifier<Student>(1)), []);
          console.log('Created entry with id:', entryId);
          expect(entryId).toBeDefined();
        });
        return Effect.provide(program, layer);
      })
      it.effect('getEntry', () => {
        const program = Effect.gen(function* () {
          const entryRepository = yield* EntriesRepositoryTag;
          const headword = 'cat-' + Math.floor(Math.random() * 10000);
          const entryId = yield* entryRepository.createEntry(Entry(Localized(LangCode('en'), headword), [
            Definition(Localized(LangCode('en'), 'A small domesticated carnivorous mammal')),
            Definition(Localized(LangCode('fr'), 'Un petit mammifère carnivore domestiqué'))
          ], Identifier<Student>(1)), []);
          const entryOpt = yield* entryRepository.getEntry(entryId);
          expect(entryOpt._tag).toBe('Some');
          if (entryOpt._tag === 'Some') {
            expect(entryOpt.value.word.s).toBe(headword);
            expect(entryOpt.value.word.langCode).toEqual(LangCode('en'));
            expect(entryOpt.value.definitions.length).toBe(2);
            expect(entryOpt.value.ownerId).toEqual(Identifier(1));
          }
        });
        return Effect.provide(program, layer);
      })
    })
  })
})
