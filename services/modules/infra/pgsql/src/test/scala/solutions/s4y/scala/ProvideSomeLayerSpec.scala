package solutions.s4y.scala

import zio.{ZIO, ZLayer}
import zio.test.{ZIOSpecDefault, assertTrue}

val sut =
  for {
    a <- ZIO.service[String] // should be "a"
    _ <- ZIO.debug(s"outer sees: $a")
    b <- (
      for {
        b <- ZIO.service[String] // should be "b"
        _ <- ZIO.debug(s"inner sees: $b")
      } yield b
    ).provideSomeLayer(ZLayer.succeed("b"))
    a2 <- ZIO.service[String] // should be "a" again
    _ <- ZIO.debug(s"back to outer sees: $a2")
  } yield (a, b, a2)

object ProvideSomeLayerSpec extends ZIOSpecDefault {
  def spec = suite("ProvideSomeLayerSpec")(
    test("provideSomeLayer should handle nested layers correctly") {
      sut
        .provideSomeLayer(
          ZLayer.succeed("a")
        )
        .flatMap { case (a, b, a2) =>
          ZIO.debug(s"Final values: a=$a, b=$b, a2=$a2").as(a, b, a2)
        }
        .map((a, b, a2) => assertTrue(a == "a" && b == "b" && a2 == "a"))
    }
  )
}
