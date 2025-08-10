package solutions.s4y.zio

import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}
import zio.{Scope, ZIO}

object ErrorSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] = {
    suite("ErrorSpec")(
      test("should map error to string") {
        val zio = ZIO
          .fail(new RuntimeException("Test error"))
          .e(e => s"Mapped: ${e.getMessage}")
        for {
          errorMessage <- zio.catchAll(s => ZIO.succeed(s))
        } yield assertTrue(
          errorMessage == """Mapped: Test error"""
        )
      }
    )
  }
}
