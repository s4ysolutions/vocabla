package solutions.s4y.vocabla.app.ports.students.settings.learn_lang

import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import solutions.s4y.vocabla.domain.{Lang, User}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Command to add a new language the student wants to learn.")
final case class AddLearnLangCommand(
    @description("Code of the language to be added.")
    langCode: Lang.Code,
    @description("Identifier of the student who wants to learn the language.")
    studentId: Identifier[User.Student]
)

object AddLearnLangCommand:
  given (using IdentifierSchema): Schema[AddLearnLangCommand] = Schema.derived
