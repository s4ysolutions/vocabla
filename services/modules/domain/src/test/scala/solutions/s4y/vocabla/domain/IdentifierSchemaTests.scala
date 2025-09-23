package solutions.s4y.vocabla.domain

import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.json.*
import zio.schema.{Schema, derived}

class IdentifierSchemaTests extends munit.FunSuite:
  trait Foo

  test("can instantiate schema for Identifier[Foo] with long internal type") {
    given IdentifierSchema with
      type ID = Long
      val schema: Schema[Long] = summon[Schema[Long]]

    val schema = summon[Schema[Identifier[Foo]]]
    assertNotEquals(schema, null)
  }

  test("can crate json encoder for Identifier[Foo] with long internal type") {
    given IdentifierSchema with
      type ID = Long
      val schema: Schema[Long] = summon[Schema[Long]]

    val id = 1L.identifier[Foo]
    val encoded = id.toJson
    assertEquals(encoded, """1""")
  }

  test("can decode json for Identifier[Foo] with long internal type") {
    given IdentifierSchema with
      type ID = Long
      val schema: Schema[Long] = summon[Schema[Long]]

    val decoded = """1""".fromJson[Identifier[Foo]]
    assertEquals(decoded, Right(1L.identifier[Foo]))
  }

  test(
    "can encoded decode a SUT class with Identifier[Foo] with long internal type"
  ) {
    given IdentifierSchema with
      type ID = Long
      val schema: Schema[Long] = summon[Schema[Long]]

    final case class SUT(id: Identifier[Foo], bar: String)

    object SUT:
      given Schema[SUT] = Schema.derived
      given JsonCodec[SUT] = DeriveJsonCodec.gen[SUT]

    val sut = SUT(1L.identifier[Foo], "baz")
    val encoded = sut.toJson
    assertEquals(encoded, """{"id":1,"bar":"baz"}""")

    val decoded = encoded.fromJson[SUT]
    assertEquals(decoded, Right(sut))
  }

  test("can create codec with implicit IdentifierSchema in scope") {
    final case class SUT(id: Identifier[Foo], bar: String)

    val identifierSchema = new IdentifierSchema {
      type ID = Long
      val schema: Schema[Long] = summon[Schema[Long]]
    }

    object SUT:
      given (using IdentifierSchema): Schema[SUT] = Schema.derived
      given (using IdentifierSchema): JsonCodec[SUT] = DeriveJsonCodec.gen[SUT]

    class Repo(using IdentifierSchema) {
      def decode(encoded: String): Either[String, SUT] =
        encoded.fromJson[SUT]
    }

    val sut = SUT(1L.identifier[Foo], "baz")
    val repo = new Repo(using identifierSchema)
    val encoded = """{"id":1,"bar":"baz"}"""
    val decoded = repo.decode(encoded)
    assertEquals(decoded, Right(sut))
  }

end IdentifierSchemaTests
