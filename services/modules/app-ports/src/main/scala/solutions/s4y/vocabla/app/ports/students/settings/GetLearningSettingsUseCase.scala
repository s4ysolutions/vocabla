package solutions.s4y.vocabla.app.ports.students.settings

import solutions.s4y.vocabla.app.ports.errors.ServiceFailure
import solutions.s4y.vocabla.domain.UserContext
import solutions.s4y.vocabla.domain.errors.NotAuthorized
import zio.ZIO
import zio.schema.annotation.description

@description(
  "Use case for retrieving the learning settings of a student by owner ID."
)
trait GetLearningSettingsUseCase:
  def apply(
      command: GetLearningSettingsCommand
  ): ZIO[
    UserContext,
    ServiceFailure | NotAuthorized,
    GetLearningSettingsResponse
  ]
