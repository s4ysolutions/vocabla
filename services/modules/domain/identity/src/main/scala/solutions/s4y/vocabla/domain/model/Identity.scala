package solutions.s4y.vocabla.domain.model

import zio.prelude.Equal

import java.util.UUID

trait Identity[E]:
  type ID
  val internal: ID
  override def toString: String = s"Identity(${internal.toString})"

  override def equals(obj: Any): Boolean = obj match
    case that: Identity[?] => this.internal == that.internal
    case _                 => false

  override def hashCode(): Int = internal.hashCode()

object Identity:
  def apply[E, I](id: I): Identity[E] = new Identity[E]:
    type ID = I
    val internal: ID = id

  trait IdConverter[DTOID] {
    def unsafeId(identity: Identity[?]): DTOID
  }

  extension (identity: Identity[?])
    def toId[DTOID](using converter: IdConverter[DTOID]): DTOID =
      converter.unsafeId(identity)

  extension [I](internal: I)
    def identity[E]: Identity[E] = Identity[E, I](internal)

  given IdConverter[UUID] with
    override def unsafeId(identity: Identity[?]): UUID =
      identity.internal match {
        case string: String => UUID.fromString(string)
        case uuid: UUID     => uuid
        case _ =>
          throw new NoSuchElementException(
            s"Can not convert ${identity.internal} to UUID, it is not a String or UUID type."
          )
      }

  given Equal[Identity[?]] = Equal.make((a, b) => a.internal == b.internal)
