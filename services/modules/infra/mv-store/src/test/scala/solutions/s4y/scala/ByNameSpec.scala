package solutions.s4y.scala

import zio.test.{ZIOSpecDefault, assert}
import zio.test.Assertion.equalTo

object ByNameSpec extends ZIOSpecDefault {
  def spec = suite("ByNameSpec")(
    test("by-name parameter evaluation") {
      var count = 0;

      def byNameOperand(): Int = {
        count += 1
        count
      }

      def byNameOperator(x: => Int): Int = {
        x
      }

      val r1 = byNameOperator(byNameOperand())
      val r2 = byNameOperator(byNameOperand())

      assert(r1)(equalTo(1)) &&
      assert(r2)(equalTo(2))
    }
  )

}
