package solutions.s4y.vocabla.error

import zio.ZIO

extension [R, A](self: zio.ZIO[R, Throwable, A])
  def e(f: Throwable => String): zio.ZIO[R, String, A] = {
    self
      .tapErrorCause { cause =>
        ZIO.logErrorCause(f(cause.squash), cause);
      }
      .mapError(f)
  }
  end e
