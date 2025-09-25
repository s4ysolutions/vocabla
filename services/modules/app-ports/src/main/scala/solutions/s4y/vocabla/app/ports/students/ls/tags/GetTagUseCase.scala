package solutions.s4y.vocabla.app.ports.students.ls.tags

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description("Use case for retrieving a tag by ID.")
trait GetTagUseCase:
  def apply(
      command: GetTagCommand
  ): ZIO[UserContext, ServiceFailure | NotAuthorized, GetTagResponse]
