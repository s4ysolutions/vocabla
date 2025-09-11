import {describe, expect, it} from 'vitest';
import {Effect, Match, Option, Schema} from 'effect';
import {ParseError} from 'effect/ParseResult';


describe('schema', () => {
  describe('SchemaAA', () => {
    const schemaAA = Schema.Struct({
      value: Schema.Number
    })

    type A = Schema.Schema.Type<typeof schemaAA>
    const a: A = {value: 42}
    void a;
    type I = Schema.Schema.Encoded<typeof schemaAA>
    const i: I = {value: 42}
    void i;

    it('encode success', () => {
      const a: A = schemaAA.make({value: 42});
      const effect: Effect.Effect<A, ParseError, never> = Schema.encode(schemaAA)(a);
      const result: A = Effect.runSync(effect)
      console.log(effect)
      console.log(result)
      expect(result).toEqual({value: 42});
    });
    it('encode failed', () => {
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const a: A = {value: '42' as any};
      const decoded: Effect.Effect<A, ParseError, never> = Schema.decode(schemaAA)(a);
      expect(() => Effect.runSync(decoded)).toThrowError();
    });
  });
  describe('SchemaAI', () => {
    type DECODED = boolean //A, decoded (from outer), can be validated, the target of decoding
    type ENCODED = 'on' | 'off' //I, encoded (to outer), the target of encoding
    const schemaAI: Schema.Schema<DECODED, ENCODED, never> = Schema.transform(
      Schema.Literal('on', 'off'), // I, ENCODED, DTO
      Schema.Boolean,//A, DECODED, DOMAIN
      {
        strict: true,
        decode: (a) => a === 'on', // from I to A, from dto to domain
        encode: (i) => i ? 'on' : 'off' // from A to I, from the subject of validation to the outer world
      }
    )

    it('types check', () => {
      type A = Schema.Schema.Type<typeof schemaAI>
      const a: A = true
      const a1: DECODED = true
      expect(a).toBe(a1)

      type I = Schema.Schema.Encoded<typeof schemaAI>
      const i: I = 'on'
      const i1: ENCODED = 'on'
      expect(i).toBe(i1)
    })

    it('decode (from outer to domain) success', () => {
      const i: ENCODED = 'on';
      const effect: Effect.Effect<DECODED, ParseError, never> = Schema.decode(schemaAI)(i);
      const result: DECODED = Effect.runSync(effect)
      expect(result).toEqual(true);
    })
    it('decode (from outer to domain) failed', () => {
      const i = 'yes';
      const decoded: Effect.Effect<DECODED, ParseError, never> = Schema.decode(schemaAI)(i as ENCODED);
      expect(() => Effect.runSync(decoded)).toThrowError();
    })
    it('encode (from domain to outer) success', () => {
      const a: DECODED = false;
      const effect: Effect.Effect<ENCODED, ParseError, never> = Schema.encode(schemaAI)(a);
      const result: ENCODED = Effect.runSync(effect)
      expect(result).toEqual('off');
    })
    it('encode (from domain to outer) failed', () => {
      const a = 'false';
      const encoded: Effect.Effect<ENCODED, ParseError, never> = Schema.encode(schemaAI)(a as unknown as DECODED);
      expect(() => Effect.runSync(encoded)).toThrowError();
    });
    it('decode sync (from outer to domain) success', () => {
      const i: ENCODED = 'on';
      const result: DECODED = Schema.decodeSync(schemaAI)(i);
      expect(result).toEqual(true);
    })
    it('decode sync (from outer to domain) failed', () => {
      const i = 'yes';
      expect(() => Schema.decodeSync(schemaAI)(i as ENCODED)).toThrowError();
    })
    it('decode unknown (from outer to domain) success', () => {
      const i: unknown = 'on';
      const effect: Effect.Effect<DECODED, ParseError, never> = Schema.decodeUnknown(schemaAI)(i);
      const result: DECODED = Effect.runSync(effect)
      expect(result).toEqual(true);
    })
    it('decode unknown (from outer to domain) failed', () => {
      const i: unknown = 'yes';
      const decoded: Effect.Effect<DECODED, ParseError, never> = Schema.decodeUnknown(schemaAI)(i);
      expect(() => Effect.runSync(decoded)).toThrowError();
    });
  });
  describe('DTO => Domain', () => {
    it('id => identifier', () => {
      type DTO = { tagId: number }
      type DOMAIN = { value: number }
      const schemaDTO: Schema.Schema<DTO> = Schema.Struct({tagId: Schema.Number})
      const schemaDomain: Schema.Schema<DOMAIN, DTO> = Schema.transform(
        schemaDTO,
        Schema.Struct({value: Schema.Number}),
        {
          strict: true,
          decode: (dto) => ({value: dto.tagId}),
          encode: (domain) => ({tagId: domain.value})
        }
      )

      const dto: DTO = {tagId: 42}
      const effectDecode: Effect.Effect<DOMAIN, ParseError, never> = Schema.decodeUnknown(schemaDomain)(dto);
      const domain: DOMAIN = Effect.runSync(effectDecode)
      expect(domain).toEqual({value: 42});
    })
  });
  describe('JSON optional fields', () => {
    type DTO = {
      payload?: string
    }
    const transformer: Schema.Schema<string, DTO> = Schema.transform(
      Schema.Struct({
        payload: Schema.optional(Schema.String)
      }),
      Schema.String,
      {
        decode: (dto) => {
          if (dto.payload !== undefined)
            return dto.payload;
          else
            return 'default';
        },
        encode: (domain) => ({payload: domain}),
        strict: true,
      })

    it('optional payload defined', () => {
      const dto1: DTO = {
        payload: 'data'
      }
      expect(Schema.decodeSync(transformer)(dto1)).toEqual('data')
    });
    it('optional payload not defined', () => {
      const dto1: DTO = {}
      expect(Schema.decodeSync(transformer)(dto1)).toEqual('default')
    });
    it('optional payload is null throws', () => {
      const dto1: DTO = {
        payload: null
      }
      expect(() => Schema.decodeSync(transformer)(dto1)).toThrow()
    });
  });
  describe('NullOr', () => {
    type DTO = {
      payload: string | null
    }
    it('NullOr(not null)', () => {
      const schema = Schema.Struct({
        payload: Schema.NullOr(Schema.String)
      })
      const dto1: DTO = {
        payload: 'data'
      }
      const decoded = Schema.decodeSync(schema)(dto1)
      expect(decoded).toEqual({payload: 'data'})
    })
    it('NullOr(null)', () => {
      const schema = Schema.Struct({
        payload: Schema.NullOr(Schema.String)
      })
      const dto: DTO = {
        payload: null
      }
      const decoded = Schema.decodeSync(schema)(dto)
      expect(decoded).toEqual({payload: null})
    })
    it('NullOr(absent)', () => {
      const schema = Schema.Struct({
        payload: Schema.NullOr(Schema.String)
      })
      const dto: DTO = {} as unknown as DTO
      expect(() => Schema.decodeSync(schema)(dto)).toThrow()
    })
  });
  describe('optionalTo*', () => {
    type DTO = {
      payload?: string
    }
    it('optionalToOptional', () => {
      const schema = Schema.Struct({
        payload: Schema.optionalToOptional(
          Schema.String,
          Schema.String,
          {
            decode: (mayBeString) => {
              if (Option.isNone(mayBeString)) {
                return Option.none()
              }
              const someString = mayBeString as Option.Some<string>
              const s = someString.value
              return Option.some('decoded: ' + s);
            },
            encode: (mayBeString) => {
              if (Option.isNone(mayBeString)) {
                return Option.none()
              }
              const s = mayBeString as Option.Some<string>
              return Option.some(`encode: ${s.value}`)
            }
          }
        )
      })
      const dto1: DTO = {
        payload: 'data'
      }
      const decoded = Schema.decodeSync(schema)(dto1)
      expect(decoded).toEqual({payload: 'decoded: data'})
      const decoded2 = Schema.decodeSync(schema)({})
      expect(decoded2).toEqual({})
    });
    it('Schema.optionalToRequired', () => {
      const schema = Schema.Struct({
        payload: Schema.optionalToRequired(
          Schema.String,
          Schema.String,
          {
            decode: (mayBeString) => {
              if (Option.isNone(mayBeString)) {
                return 'default'
              }
              const someString = mayBeString as Option.Some<string>
              const s = someString.value
              return 'decoded: ' + s;
            },
            encode: (s) => Option.some(`encode: ${s}`)
          }
        )
      })
      const dto1: DTO = {
        payload: 'data'
      }
      const decoded = Schema.decodeSync(schema)(dto1)
      expect(decoded).toEqual({payload: 'decoded: data'})
      const decoded2 = Schema.decodeSync(schema)({})
      expect(decoded2).toEqual({payload: 'default'})
    })
  });
  describe('optional nullable fields', () => {
    type DTO = {
      payload?: string | null
    }
    const transformer: Schema.Schema<string | null, DTO> = Schema.transform(
      Schema.Struct({
        payload: Schema.optional(Schema.UndefinedOr(Schema.NullOr(Schema.String)))
      }),
      Schema.NullOr(Schema.String),
      {
        decode: (dto) => {
          if (dto.payload !== undefined)
            return dto.payload;
          else
            return 'default';
        },
        encode: (domain) => ({payload: domain}),
        strict: true,
      })

    it('optional payload defined', () => {
      const dto1: DTO = {
        payload: 'data'
      }
      expect(Schema.decodeSync(transformer)(dto1)).toEqual('data')
    });
    it('optional payload not defined', () => {
      const dto1: DTO = {}
      expect(Schema.decodeSync(transformer)(dto1)).toEqual('default')
    });
    it('optional payload is null', () => {
      const dto1: DTO = {
        payload: null
      }
      expect(Schema.decodeSync(transformer)(dto1)).toEqual(null)
    })
    it('optional transform{}', () => {
      type Domain = {
        label: string
      }
      const schemaDomain = Schema.Struct({
        label: Schema.String
      })
      type Dto = {
        domain?: { tag: number } | null
      }
      const schemaDto = Schema.Struct({
        //domain: Schema.optionalWith(Schema.NullOr(Schema.Struct({tag: Schema.Number})),{})// {exact: true})
        domain: Schema.optional(Schema.NullOr(Schema.Struct({tag: Schema.Number})))// {exact: true})
      })

      const schemaDomainFromDto: Schema.Schema<Domain, Dto> = Schema.transform(
        schemaDto,
        schemaDomain,
        {
          strict: true,
          decode: (dto) => {
            if (dto.domain === undefined || dto.domain === null) {
              throw new Error('domain is required')
            }
            return {label: `tag:${dto.domain.tag}`}
          },
          encode: (domain) => {
            return {domain: {tag: parseInt(domain.label.replace('tag:', ''))}}
          }
        }
      )

      const dto: Dto = {domain: {tag: 42}}
      const domain: Domain = Schema.decodeSync(schemaDomainFromDto)(dto)
      expect(domain).toEqual({label: 'tag:42'})

      const dtoNull: Dto = {domain: null}
      expect(() => Schema.decodeSync(schemaDomainFromDto)(dtoNull)).toThrow()

      const dtoUndefined: Dto = {}
      expect(() => Schema.decodeSync(schemaDomainFromDto)(dtoUndefined)).toThrow()

      const dtoMissed: Dto = {}
      expect(() => Schema.decodeSync(schemaDomainFromDto)(dtoMissed)).toThrow()
    });
  })
  describe('pick', () => {
    it('pick prop{}', () => {
      const schema = Schema.Struct({
        prop: Schema.optional(Schema.NullOr(Schema.String))
      })
      const schemaPicked = schema.pick('prop')
      const decoded = Schema.decodeSync(schemaPicked)({})
      expect(decoded).toEqual({})
    })
    it('pick prop (data)', () => {
      const schema = Schema.Struct({
        prop: Schema.optional(Schema.NullOr(Schema.String))
      })
      const schemaPicked = schema.pick('prop')
      const decoded = Schema.decodeSync(schemaPicked)({prop: 'data'})
      expect(decoded).toEqual({prop: 'data'})
    })
    it('pick prop (null)', () => {
      const schema = Schema.Struct({
        prop: Schema.optional(Schema.NullOr(Schema.String))
      })
      const schemaPicked = schema.pick('prop')
      const decoded = Schema.decodeSync(schemaPicked)({prop: null})
      expect(decoded).toEqual({prop: null})
    })
  });
  describe('optionalWith', () => {
    it('optionalWith ', () => {
      const schemaDto = Schema.Struct({
        domain: Schema.optionalWith(Schema.NullOr(Schema.Struct({tag: Schema.Number})), {})
      })
      //const schemaDtoAs = Schema.asSchema(schemaDto)
      type DtoInexact = Schema.Schema.Type<typeof schemaDto>
      const dto1: DtoInexact = {}
      const dto2: DtoInexact = {domain: null}
      const dto3: DtoInexact = {domain: {tag: 42}}
      const dto4: DtoInexact = {domain: undefined}

      const schemaDtoExact = Schema.Struct({
        domain: Schema.optionalWith(Schema.NullOr(Schema.Struct({tag: Schema.Number})), {exact: true})
      })
      type DtoExact = Schema.Schema.Type<typeof schemaDtoExact>
      const dtoE1: DtoExact = {}
      const dtoE2: DtoExact = {domain: null}
      const dtoE3: DtoExact = {domain: {tag: 42}}
      const dtoE4: DtoExact = {domain: undefined}

      expect(Schema.decodeSync(schemaDto)(dto1)).toEqual({})
      expect(Schema.decodeSync(schemaDto)(dto2)).toEqual({domain: null})
      expect(Schema.decodeSync(schemaDto)(dto3)).toEqual({domain: {tag: 42}})
      expect(Schema.decodeSync(schemaDto)(dto4)).toEqual({domain: undefined})

      expect(Schema.decodeSync(schemaDtoExact)(dtoE1)).toEqual({})
      expect(Schema.decodeSync(schemaDtoExact)(dtoE2)).toEqual({domain: null})
      expect(Schema.decodeSync(schemaDtoExact)(dtoE3)).toEqual({domain: {tag: 42}})
      expect(() => Schema.decodeSync(schemaDtoExact)(dtoE4)).toThrow() // undefined is not allowed in exact mode
    })
  })
  describe('Option', () => {
    it('Option', () => {
      const schemaOption = Schema.Option(Schema.String)
      type OptionString = Schema.Schema.Type<typeof schemaOption>
      expect(Schema.decodeSync(schemaOption)({_tag: 'None'})).toEqual(Option.none())
      expect(Schema.decodeSync(schemaOption)({_tag: 'Some', value: 'data'})).toEqual(Option.some('data'))
      expect(() => Schema.decodeSync(schemaOption)({} as OptionString)).toThrow()
      expect(() => Schema.decodeSync(schemaOption)({type: 'Some', value: 42} as unknown as OptionString)).toThrow()
    })
    it('Option as transformation', () => {
      const schemaOption: Schema.Schema<Option.Option<string>, string> = Schema.transform(
        Schema.String,
        Schema.Option(Schema.String),
        {
          decode: (fromA) => ({_tag: 'Some', value: fromA} as const),
          encode: (toI) => {
            const encoded: string = Match.value(toI).pipe(
              Match.tag('None', () => 'none'),
              Match.tag('Some', (some) => some.value),
              Match.exhaustive
            )
            return encoded;
          },
          strict: true
        }
      )
      const output = Schema.decodeSync(schemaOption)('data')
      expect(Option.isSome(output))
      expect(output).toEqual({value: 'data'})
    })
    it('OptionFromSelf ', () => {
      const schemaTransformed: Schema.Schema<string, number> = Schema.transform(
        Schema.Number,
        Schema.String,
        {
          decode: (n) => n.toString(),
          encode: (s) => parseInt(s),
          strict: true
        }
      )
      const schemaOptionFromSelf = Schema.OptionFromSelf(schemaTransformed)
      const a = Option.some('42')
      const i = Option.some(42)
      expect(Schema.decodeSync(schemaOptionFromSelf)(i)).toEqual(a)
      expect(Schema.encodeSync(schemaOptionFromSelf)(Option.none())).toEqual(Option.none())
    })
    it('OptionFromUndefined', () => {
      type DTO = {
        payload?: string | null
      }
      type Domain = Option.Option<string>
      const domainSome: Domain = Option.some('data')
      const domainNone: Domain = Option.none()

      expect(domainSome).not.toBe(domainNone)
      expect(domainSome).toEqual(Option.some('data'))
      expect(domainNone).toEqual(Option.none())

      const transformer: Schema.Schema<Domain, DTO> = Schema.transform(
        Schema.Struct({
          payload: Schema.optional(Schema.NullOr(Schema.String))
        }),
        Schema.Option(Schema.String),
        {
          decode: (dto) => {
            if (dto.payload === undefined || dto.payload === null)
              return Option.none()
            else
              return Option.some(dto.payload);
          },
          encode: (domain) => {
            return Match.value(domain).pipe(
              Match.when({_tag: 'None'}, () => ({payload: null})),
              Match.when({_tag: 'Some'}, (s) => ({payload: s.value})),
              Match.exhaustive
            )
          },
          strict: true,
        })

      expect(Schema.decodeSync(transformer)({payload: 'data'})).toEqual(Option.some('data'))
      /*
      expect(Schema.decodeSync(transformer)({})).toEqual(Option.none())
      expect(Schema.decodeSync(transformer)({payload: null})).toEqual(Option.none())

      expect(Schema.encodeSync(transformer)(Option.some('data'))).toEqual({payload: 'data'})
      expect(Schema.encodeSync(transformer)(Option.none())).toEqual({payload: null})
       */
    })
  })
})
