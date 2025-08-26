package solutions.s4y.vocabla.domain

import zio.{Chunk, NonEmptyChunk}
import zio.prelude.{Validation, ZValidation}
import zio.prelude.ZValidation.Failure

class PreludeValidationTests extends munit.FunSuite:
  test("Prelude validations With succeed") {
    val v1 = Validation.succeed(())
    val v2 = Validation.succeed(())

    val combined = Validation.validateWith(v1, v2)((_, _) => ())
    assert(combined.isSuccess)
  }

  test("Prelude validations With fail") {
    val v1 = Validation.fail("Error 1")
    val v2 = Validation.succeed(())

    val combined = Validation.validateWith(v1, v2)((_, _) => ())
    val errors = combined.fold(
      fail => fail,
      _ => NonEmptyChunk("succeed")
    )
    assertEquals(errors, NonEmptyChunk("Error 1"))
    assertEquals(combined, Validation.fail("Error 1"))
  }

  test("Prelude validations With both succeed") {
    val v1 = Validation.succeed("a")
    val v2 = Validation.succeed("b")

    val combined = Validation.validateWith(v1, v2)(_ ++ _)
    val errors = combined.fold(
      fail => fail,
      _ => NonEmptyChunk("succeed")
    )
    assertEquals(errors, NonEmptyChunk("succeed"))
    assertEquals(combined, Validation.succeed("ab"))
  }

  test("Prelude validations With both fail") {
    val v1 = Validation.fail("Error 1")
    val v2 = Validation.fail("Error 2")

    val combined = Validation.validateWith(v1, v2)((_, _) => ())
    val errors = combined.fold(
      fail => fail,
      _ => Chunk("succeed")
    )
    assertEquals(errors, NonEmptyChunk("Error 1", "Error 2"))
  }
