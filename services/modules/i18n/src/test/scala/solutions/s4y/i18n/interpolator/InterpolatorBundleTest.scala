package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesBundleResolver, TranslationResolver, t}

import java.util.Locale

class InterpolatorBundleTest extends munit.FunSuite {
  given resolver: TranslationResolver =
    ResourcesBundleResolver("messages")

  test("Hello, World!") {
    given locale: Locale = Locale.ENGLISH
    val greeting = t"hello.world"
    assertEquals(greeting.toString, "Hello, World!(en)")
  }
  test("Hello, $user") {
    given locale: Locale = Locale.ENGLISH
    val greeting = t"hello.world.name" ("John")
    assertEquals(greeting.toString, "Hello, John!(en)")
  }
  test("Hello, World!(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val greeting = t"hello.world"
    assertEquals(greeting.toString, "Здраво, свете!")
  }
  test("Hello, $user(sr)") {
    given locale: Locale = Locale.forLanguageTag("sr")
    val name = "John"
    val greeting = t"hello.world.name" ("John")
    assertEquals(greeting.toString, "Здраво, John!")
  }
  test("Hello, $user $another ") {
    given locale: Locale = Locale.ENGLISH
    val greeting = t"hello.world.names" ("John", "Bob")
    println(greeting)
    assertEquals(greeting.toString, "Hello, John and Bob!(en)")
  }
}
