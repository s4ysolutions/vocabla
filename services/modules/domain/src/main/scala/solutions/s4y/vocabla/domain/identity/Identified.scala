package solutions.s4y.vocabla.domain.identity

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
