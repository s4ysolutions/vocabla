package solutions.s4y.vocabla.app.ports.students.settings.known_lang

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Command to remove a language the student knows.")
final case class RemoveKnownLangCommand(
    @description("Code of the language to be removed from known languages.")
    langCode: Lang.Code,
    @description("Identifier of the student who wants to remove the known language.")
    studentId: Identifier[User.Student]
)

object RemoveKnownLangCommand:
  given (using IdentifierSchema): Schema[RemoveKnownLangCommand] = Schema.derived
