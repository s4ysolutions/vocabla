package solutions.s4y.vocabla.app.ports

import solutions.s4y.app.{AppCommand, UseCase}
import solutions.s4y.vocabla.domain.{Tag, Entry}
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.IO

final case class TagEntryCommand(
    entryId: Identifier[Entry],
    tagId: Identifier[Tag]
) extends AppCommand:
  override type Result = IO[String, Boolean]

trait TagEntryUseCase extends UseCase[TagEntryCommand]:
  override def apply(command: TagEntryCommand): IO[String, Boolean]

