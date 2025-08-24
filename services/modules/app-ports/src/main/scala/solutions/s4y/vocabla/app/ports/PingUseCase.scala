package solutions.s4y.vocabla.app.ports

import zio.ZIO
import zio.schema.annotation.{description, validate}
import zio.schema.validation.Validation
import zio.schema.{DeriveSchema, Schema}

final case class PingCommand(
    @description(
      "The payload message to echo back. Must be at least 2 characters long."
    )
    @validate(Validation.minLength(2))
    payload: String
)

object PingCommand:
  type Response = String
  given Schema[PingCommand] = DeriveSchema.gen[PingCommand]

trait PingUseCase:
  def apply[R](command: PingCommand): ZIO[R, String, PingCommand.Response]
