package solutions.s4y.vocabla.domain.identity

import zio.prelude.{Equal, EqualOps, Equivalence}
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
    def asIdentifier[E1]: Identifier[E1] =
      Identifier[E1, identifier.ID](identifier.internal)

  given [E](using is: IdentifierSchema): Schema[Identifier[E]] =
    is.schema.transform[Identifier[E]](
      id => Identifier[E, is.ID](id),
      (identity: Identifier[E]) => identity.as[is.ID]
    )

  given [E, I: Equal]: Equal[Identifier[E]] =
    Equal.make((a, b) =>
      a.internal == b.internal
    ) // .asInstanceOf[a.ID] ) // TODO: === causes Cast error

  given [E, I]: Equivalence[Identifier[E], I] = Equivalence(
    (identifier: Identifier[E]) => identifier.as[I],
    (id: I) => Identifier[E, I](id)
  )
