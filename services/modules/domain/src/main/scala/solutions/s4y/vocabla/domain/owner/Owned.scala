package solutions.s4y.vocabla.domain.owner

import solutions.s4y.vocabla.domain.identity.Identifier
import zio.prelude.EqualOps

trait Owned[OWNER]:
  val ownerId: Identifier[OWNER]
  def isOwnedBy(ownerId: Identifier[OWNER]): Boolean = this.ownerId === ownerId
