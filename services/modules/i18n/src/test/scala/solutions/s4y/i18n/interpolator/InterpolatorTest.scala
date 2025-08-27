package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesResolver, TranslationResolver, t}

import java.util.Locale

class InterpolatorTest extends munit.FunSuite {
  given resolver: TranslationResolver =
    ResourcesResolver("messages", Locale.ENGLISH)
  test("Hello, World!") {
    given locale: Locale = Locale.ENGLISH
    val greeting = t"Hello, World!"
    assertEquals(greeting.toString, "Hello, World!(en)")
  }
  test("Hello, $user") {
    given locale: Locale = Locale.ENGLISH
    val name = "John"
    val greeting = t"Hello, ${name}!"
    assertEquals(greeting.toString, "Hello, John!(en)")
  }
  test("Hello, World!(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val greeting = t"Hello, World!"
    assertEquals(greeting.toString, "Здраво, свете!")
  }
  test("Hello, $user(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val name = "John"
    val greeting = t"Hello, ${name}!"
    assertEquals(greeting.toString, "Здраво, John!")
  }

  /*
  test("items.count") {
    given locale: Locale = Locale.ENGLISH
    val itemCount = t"items.count" (5, "books")
    assertEquals(itemCount.toString, "You have 5 books")
  }
  test("directTemplate") {
    given locale: Locale = Locale.ENGLISH
    val name = "Alice"
    val count = 3
    val directTemplate = l"Hello $name, you have $count messages"
    assertEquals(directTemplate.toString, "Hello Alice, you have 3 messages")
  }
   */
}
