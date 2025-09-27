package solutions.s4y.vocabla.domain.identity

import zio.Tag
import zio.json.JsonCodec
import zio.prelude.{Equal, Equivalence}
import zio.schema.{Schema, TypeId}

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

  given [E](using is: IdentifierSchema, ct: Tag[E]): Schema[Identifier[E]] =
    val typeName = ct.tag.shortName
    val customId = TypeId.parse(s"Identifier$typeName")
    is.schema
      .transform[Identifier[E]](
        id => Identifier[E, is.ID](id),
        (identity: Identifier[E]) => identity.as[is.ID]
      )
      .annotate(customId)
  /*
  given [E](using is: IdentifierSchema, ct: Tag[E]): Schema[Identifier[E]] = {
    val typeName = ct.tag.shortName
    val customId = TypeId.parse(s"Identifier$typeName")
    Schema.CaseClass1(
      customId,
      Schema.Field(
        "internal",
        is.schema,
        get0 = (id: Identifier[E]) => id.as[is.ID],
        set0 = (_, newValue: is.ID) => Identifier[E, is.ID](newValue)
      ),
      (value: is.ID) => Identifier[E, is.ID](value),
      Chunk.empty
    )

  }*/
  /*
  given identifierSchemaWithTypeId[E](using
      is: IdentifierSchema,
      ct: Tag[E]
  ): Schema[Identifier[E]] = {
    val typeName = ct.tag.shortName
    val customId = TypeId.parse(s"Identifier$typeName")

    // Create the base schema with custom TypeId, then transform
    val baseSchema = is.schema.annotate(customId)
    val transformedSchema = baseSchema.transform[Identifier[E]](
      id => Identifier[E, is.ID](id),
      (identity: Identifier[E]) => identity.as[is.ID]
    )

    transformedSchema.annotate(customId)
  }
   */

  given [E](using schema: Schema[Identifier[E]]): JsonCodec[Identifier[E]] = {
    zio.schema.codec.JsonCodec.jsonCodec(schema)
  }

  given [E, I: Equal]: Equal[Identifier[E]] =
    Equal.make((a, b) =>
      a.internal == b.internal
    ) // .asInstanceOf[a.ID] ) // TODO: === causes Cast error

  given [E, I]: Equivalence[Identifier[E], I] = Equivalence(
    (identifier: Identifier[E]) => identifier.as[I],
    (id: I) => Identifier[E, I](id)
  )
