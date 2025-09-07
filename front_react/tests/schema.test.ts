import {describe, expect, it} from 'vitest';
import {Effect, Schema, Option} from 'effect';
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
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
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
              const r= Option.some('decoded: '+ s)
              return r;
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
    it('Schema.optionalToRequired',()=>{
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
              const r= 'decoded: '+ s
              return r;
            },
            encode: (s) => `encode: ${s}`
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
  })
})
