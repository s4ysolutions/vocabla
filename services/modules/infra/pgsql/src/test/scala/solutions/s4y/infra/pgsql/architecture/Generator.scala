package solutions.s4y.infra.pgsql.architecture

import zio.{Ref, UIO}

trait Generator:
  def next: UIO[String]

trait GeneratorS:
  def next: String

object Generator:
  val generator: UIO[Generator] =
    Ref.make(0).map { ref =>
      new Generator:
        def next: UIO[String] =
          ref.updateAndGet(_ + 1).map { nextValue =>
            s"Generated ID: $nextValue"
          }
    }

  val generatorS: GeneratorS =
    new GeneratorS:
      var count: Int = 0
      def next: String =
        count = count + 1
        s"Generated ID: $count"
