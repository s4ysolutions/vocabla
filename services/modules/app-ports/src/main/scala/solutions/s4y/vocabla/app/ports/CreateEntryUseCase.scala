package solutions.s4y.vocabla.app.ports

import solutions.s4y.app.{AppCommand, UseCase}
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import solutions.s4y.vocabla.domain.{Entry, Student, Tag}
import zio.{Chunk, IO}

final case class CreateEntryCommand(
    entry: Entry,
    tagsIds: Chunk[Identified[Tag]],
    ownerId: Identifier[Student]
) extends AppCommand:
  override type Result = IO[String, Identifier[Entry]]

trait CreateEntryUseCase extends UseCase[CreateEntryCommand]:
  override def apply(command: CreateEntryCommand): IO[String, Identifier[Entry]]
