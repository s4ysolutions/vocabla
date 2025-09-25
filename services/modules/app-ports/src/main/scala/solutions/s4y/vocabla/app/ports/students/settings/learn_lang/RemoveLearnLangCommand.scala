package solutions.s4y.vocabla.app.ports.students.settings.learn_lang

import solutions.s4y.vocabla.domain.{Lang, User}
import solutions.s4y.vocabla.domain.identity.{Identifier, IdentifierSchema}
import zio.schema.annotation.description
import zio.schema.{Schema, derived}

@description("Command to remove a language the student wants to learn.")
final case class RemoveLearnLangCommand(
    @description("Code of the language to be removed from learning languages.")
    langCode: Lang.Code,
    @description("Identifier of the student who wants to stop learning the language.")
    studentId: Identifier[User.Student]
)

object RemoveLearnLangCommand:
  given (using IdentifierSchema): Schema[RemoveLearnLangCommand] = Schema.derived

