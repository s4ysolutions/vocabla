package solutions.s4y.vocabla.app.ports

import solutions.s4y.app.{AppCommand, UseCase}
import solutions.s4y.vocabla.domain.{Student, Tag}
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.IO

final case class CreateTagCommand(
    tag: Tag,
    ownerId: Identifier[Student]
) extends AppCommand:
  override type Result = IO[String, Identifier[Tag]]
  
trait CreateTagUseCase extends UseCase[CreateTagCommand]:
  override def apply(command: CreateTagCommand): IO[String, Identifier[Tag]]
