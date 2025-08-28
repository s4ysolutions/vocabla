package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesStringsResolver, TranslationKey}

import java.util.Locale

class ResourcesStringsResolverTest extends munit.FunSuite:

  test("Hello, World!") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesStringsResolver("/messages", Locale.ENGLISH)
    val string =
      resolver.resolve(locale, TranslationKey("Hello, World!"))
    assertEquals(string, "Hello, World!(en)")
  }

  test("Hello, $user!") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesStringsResolver("/messages", Locale.ENGLISH)
    val name = "John"
    val string =
      resolver.resolve(locale, TranslationKey("Hello, {}!"), "John")
    assertEquals(string, "Hello, John!(en)")
  }

  test("Hello, World!(sr)") {
    val locale = Locale.forLanguageTag("sr")
    val resolver = ResourcesStringsResolver("/messages", Locale.ENGLISH)
    val string =
      resolver.resolve(locale, TranslationKey("Hello, World!"))
    assertEquals(string, "Здраво, свете!")
  }

  test("Hello, $user!(sr)") {
    val locale = Locale.forLanguageTag("sr")
    val resolver = ResourcesStringsResolver("/messages", Locale.ENGLISH)
    val name = "John"
    val string =
      resolver.resolve(locale, TranslationKey("Hello, {}!"), "John")
    assertEquals(string, "Здраво, John!")
  }

end ResourcesStringsResolverTest
