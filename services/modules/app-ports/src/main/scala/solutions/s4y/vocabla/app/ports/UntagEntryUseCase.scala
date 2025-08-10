package solutions.s4y.vocabla.app.ports

import solutions.s4y.app.{AppCommand, UseCase}
import solutions.s4y.vocabla.domain.identity.Identifier
import solutions.s4y.vocabla.domain.{Entry, Tag}
import zio.IO

final case class UntagEntryCommand(
    entryId: Identifier[Entry],
    tagId: Identifier[Tag]
) extends AppCommand:
  override type Result = IO[String, Boolean]

trait UntagEntryUseCase extends UseCase[UntagEntryCommand]:
  override def apply(command: UntagEntryCommand): IO[String, Boolean]
