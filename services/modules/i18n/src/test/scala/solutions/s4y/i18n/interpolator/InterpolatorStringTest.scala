package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesStringsResolver, TranslationResolver, t}

import java.util.Locale

class InterpolatorStringTest extends munit.FunSuite {
  given resolver: TranslationResolver =
    ResourcesStringsResolver("messages", Locale.ENGLISH)
  test("Hello, World!") {
    given locale: Locale = Locale.ENGLISH
    val greeting = t"Hello, World!"
    assertEquals(greeting.toString, "Hello, World!(en)")
  }
  test("Hello, $user") {
    given locale: Locale = Locale.ENGLISH
    val name = "John"
    val greeting = t"Hello, $name!"
    assertEquals(greeting.toString, "Hello, John!(en)")
  }
  test("Hello, $user and another") {
    given locale: Locale = Locale.ENGLISH
    val name = "John"
    val name2 = "Bob"
    val greeting = t"Hello, $name and $name2!"
    assertEquals(greeting.toString, "Hello, John and Bob!(en)")
  }
  test("Hello, World!(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val greeting = t"Hello, World!"
    assertEquals(greeting.toString, "Здраво, свете!")
  }
  test("Hello, $user(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val name = "John"
    val greeting = t"Hello, $name!"
    assertEquals(greeting.toString, "Здраво, John!")
  }
}
