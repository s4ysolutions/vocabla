package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Command to add a new language the student already knows.")
final case class AddKnownLangCommand(
    @description("Code of the language to be added as known.")
    langCode: Lang.Code,
    @description("Identifier of the student who knows the language.")
    studentId: Identifier[User.Student]
)

object AddKnownLangCommand:
  given (using IdentifierSchema): Schema[AddKnownLangCommand] = Schema.derived
