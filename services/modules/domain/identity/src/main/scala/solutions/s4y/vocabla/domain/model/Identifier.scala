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

trait IdentityImpl[I]

object Identifier:
  def apply[E, I](id: I): Identifier[E] = new Identifier[E]:
    type ID = I
    val internal: ID = id

  extension [I](internal: I)
    def identity[E]: Identifier[E] = Identifier[E, I](internal)

  extension [E](identifier: Identifier[E])
    def as[I]: I = identifier.internal.asInstanceOf[I]

  given [E, I: Schema]: Schema[Identifier[E]] =
    Schema[I].transform[Identifier[E]](
      Identifier[E, I],
      (identity: Identifier[E]) => identity.as[I]
    )

  given [E, I: Equal](using eqi: Equal[I]): Equal[Identifier[E]] =
    (a, b) => eqi.equal(a.as[I], b.as[I])

  given [E, I]: Equivalence[Identifier[E], I] = Equivalence(
    (identity: Identifier[E]) => identity.as[I],
    (id: I) => Identifier[E, I](id)
  )
