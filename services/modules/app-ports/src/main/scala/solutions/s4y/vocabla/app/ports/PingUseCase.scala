package solutions.s4y.vocabla.app.ports

import zio.ZIO
import zio.schema.{DeriveSchema, Schema}
import zio.schema.annotation.validate
import zio.schema.validation.Validation

final case class PingCommand(
    @validate(Validation.minLength(2)) payload: String
)

object PingCommand:
  type Response = String
  given Schema[PingCommand] = DeriveSchema.gen[PingCommand]

trait PingUseCase:
  def apply[R](command: PingCommand): ZIO[R, String, PingCommand.Response]
