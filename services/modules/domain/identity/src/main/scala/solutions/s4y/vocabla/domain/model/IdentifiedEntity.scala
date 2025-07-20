package solutions.s4y.vocabla.domain.model

import zio.prelude.Equal

case class IdentifiedEntity[E](id: Identifier[E], e: E)

object IdentifiedEntity:
  given [E](using
            eqId: Equal[Identifier[E]],
            eqE: Equal[E]
  ): Equal[IdentifiedEntity[E]] =
    (a, b) =>
      eqId.equal(a.id, b.id) && eqE.equal(a.e, b.e)
