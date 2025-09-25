package solutions.s4y.vocabla.app.ports.students.ls.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for deleting a tag by ID.")
trait DeleteTagUseCase:
  def apply(
      command: DeleteTagCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, DeleteTagResponse]
