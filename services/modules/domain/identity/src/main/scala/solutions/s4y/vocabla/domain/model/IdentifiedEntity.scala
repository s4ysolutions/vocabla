package solutions.s4y.vocabla.domain.model

import zio.prelude.Equal

case class IdentifiedEntity[E](identity: Identity[E], entity: E)

object IdentifiedEntity:
  given [E](using
      eqId: Equal[Identity[E]],
      eqE: Equal[E]
  ): Equal[IdentifiedEntity[E]] =
    Equal.make { (a, b) =>
      eqId.equal(a.identity, b.identity) &&
      eqE.equal(a.entity, b.entity)
    }
