package solutions.s4y.vocabla.app.ports

import solutions.s4y.app.{AppCommand, UseCase}
import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.Identifier
import zio.IO

final case class DeleteTagCommand(
    tagId: Identifier[Tag]
) extends AppCommand:
  override type Result = IO[String, Boolean]

trait DeleteTagUseCase extends UseCase[DeleteTagCommand]:
  override def apply(command: DeleteTagCommand): IO[String, Boolean]

