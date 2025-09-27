package solutions.s4y.vocabla.domain.identity

import zio.Chunk
import zio.json.JsonCodec
import zio.prelude.Equal
import zio.schema.{Schema, TypeId}
final case class Identified[E](id: Identifier[E], e: E)

object Identified:
  /*
  given [E: Schema](using Schema[Identifier[E]]): Schema[Identified[E]] =
    DeriveSchema.gen[Identified[E]]
   */

  given [E](using
      eSchema: Schema[E],
      idSchema: Schema[Identifier[E]],
      ct: zio.Tag[E]
  ): Schema[Identified[E]] = {
    val typeName = ct.tag.shortName
    val customTypeId = TypeId.parse(s"Identified$typeName")

    Schema.CaseClass2(
      customTypeId,
      Schema.Field(
        "id",
        idSchema,
        get0 = _.id,
        set0 = (obj, newId) => obj.copy(id = newId)
      ),
      Schema.Field(
        "e",
        eSchema,
        get0 = _.e,
        set0 = (obj, newE) => obj.copy(e = newE)
      ),
      (id: Identifier[E], e: E) => Identified(id, e),
      Chunk.empty
    )
  }

  given [E](using
      eqId: Equal[Identifier[E]],
      eqE: Equal[E]
  ): Equal[Identified[E]] =
    (a, b) => eqId.equal(a.id, b.id) && eqE.equal(a.e, b.e)

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
