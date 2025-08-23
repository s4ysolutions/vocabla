package solutions.s4y.infra.pgsql.composite

import scala.util.matching.Regex

object Patterns {
  // val typeOf2: Regex = """\((.*),(.*)\)""".r
  val typeOf2: Regex = """^\("?([^",)]*)"?,"?([^",)]*)"?\)$""".r
}
