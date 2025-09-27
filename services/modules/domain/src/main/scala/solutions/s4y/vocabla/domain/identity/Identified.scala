package solutions.s4y.vocabla.domain.identity

import zio.json.JsonCodec
import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}
final case class Identified[E](id: Identifier[E], e: E)

object Identified:
  given [E](using
      eqId: Equal[Identifier[E]],
      eqE: Equal[E]
  ): Equal[Identified[E]] =
    (a, b) => eqId.equal(a.id, b.id) && eqE.equal(a.e, b.e)

  given [E: Schema](using Schema[Identifier[E]]): Schema[Identified[E]] =
    DeriveSchema.gen[Identified[E]]
  given [E](using schema: Schema[Identified[E]]): JsonCodec[Identified[E]] = {
    zio.schema.codec.JsonCodec.jsonCodec(schema)
  }
  /*
   for sake of less verbose JSON (no field names)

  given [E](using
      idCodec: JsonCodec[Identifier[E]],
      eCodec: JsonCodec[E]
  ): JsonCodec[Identified[E]] =
    JsonCodec
      .tuple2[Identifier[E], E]
      .transform[Identified[E]](
        (id, e) => Identified(id, e),
        identified => (identified.id, identified.e)
      )
   */
