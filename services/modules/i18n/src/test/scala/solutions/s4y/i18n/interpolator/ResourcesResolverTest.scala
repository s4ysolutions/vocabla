package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesResolver, TranslationKey}

import java.util.Locale

class ResourcesResolverTest extends munit.FunSuite:

  test("Hello, World!") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesResolver("/messages", Locale.ENGLISH)
    val string =
      resolver.resolve(TranslationKey("Hello, World!"), Array.empty, locale)
    assertEquals(string, "Hello, World!(en)")
  }

  test("Hello, $user!") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesResolver("/messages", Locale.ENGLISH)
    val name = "John"
    val string =
      resolver.resolve(TranslationKey("Hello, {}!"), Array("John"), locale)
    assertEquals(string, "Hello, John!(en)")
  }

  test("Hello, World!(sr)") {
    val locale = Locale.forLanguageTag("sr")
    val resolver = ResourcesResolver("/messages", Locale.ENGLISH)
    val string =
      resolver.resolve(TranslationKey("Hello, World!"), Array.empty, locale)
    assertEquals(string, "Здраво, свете!")
  }

  test("Hello, $user!(sr)") {
    val locale = Locale.forLanguageTag("sr")
    val resolver = ResourcesResolver("/messages", Locale.ENGLISH)
    val name = "John"
    val string =
      resolver.resolve(TranslationKey("Hello, {}!"), Array("John"), locale)
    assertEquals(string, "Здраво, John!")
  }

end ResourcesResolverTest
