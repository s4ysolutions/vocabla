package solutions.s4y.vocabla.domain.model

import zio.prelude.{Equal, Equivalence}
import zio.schema.Schema

trait Identifier[E]:
  type ID
  protected val internal: ID

  override def equals(obj: Any): Boolean = obj match
    case other: Identifier[?] => this.internal == other.internal
    case _                    => false

  override def hashCode(): Int = internal.hashCode()

  override def toString: String = s"Identifier(${internal.toString})"

object Identifier:
  def apply[E, I](id: I): Identifier[E] = new Identifier[E]:
    type ID = I
    val internal: ID = id

  extension [I](internal: I)
    def identifier[E]: Identifier[E] = Identifier[E, I](internal)

  extension [E](identifier: Identifier[E])
    def as[I]: I = identifier.internal.asInstanceOf[I]

  given [E](using is: IdentifierSchema): Schema[Identifier[E]] =
    is.schema.transform[Identifier[E]](
      id => Identifier[E, is.ID](id),
      (identity: Identifier[E]) => identity.as[is.ID]
    )

  given [E, I: Equal](using eqi: Equal[I]): Equal[Identifier[E]] =
    (a, b) => eqi.equal(a.as[I], b.as[I])

  given [E, I]: Equivalence[Identifier[E], I] = Equivalence(
    (identity: Identifier[E]) => identity.as[I],
    (id: I) => Identifier[E, I](id)
  )
