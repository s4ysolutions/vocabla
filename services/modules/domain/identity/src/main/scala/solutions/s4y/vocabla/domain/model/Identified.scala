package solutions.s4y.vocabla.domain.model

import zio.prelude.Equal
import zio.schema.{DeriveSchema, Schema}

case class Identified[E](id: Identifier[E], e: E)

object Identified:
  given [E](using
      eqId: Equal[Identifier[E]],
      eqE: Equal[E]
  ): Equal[Identified[E]] =
    (a, b) => eqId.equal(a.id, b.id) && eqE.equal(a.e, b.e)

  given [E: Schema](using IdentifierSchema): Schema[Identified[E]] =
    DeriveSchema.gen[Identified[E]]

  given [E: Schema](using IdentifierSchema): Schema[Seq[Identified[E]]] = {
    val schema = summon[Schema[List[Identified[E]]]]
    schema.transform(_.toSeq, _.toList)
  }
