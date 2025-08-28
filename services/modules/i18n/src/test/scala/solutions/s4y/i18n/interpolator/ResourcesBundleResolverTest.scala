package solutions.s4y.i18n.interpolator

import solutions.s4y.i18n.{ResourcesBundleResolver, TranslationKey}

import java.util.Locale

class ResourcesBundleResolverTest extends munit.FunSuite:
  test("hello.world en") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesBundleResolver("messages")
    val string =
      resolver.resolve(locale, TranslationKey("hello.world"))
    assertEquals(string, "Hello, World!(en)")
  }

  test("hello.world.name en") {
    val locale = Locale.ENGLISH
    val resolver = ResourcesBundleResolver("messages")
    val string =
      resolver.resolve(
        locale,
        TranslationKey("hello.world.name"),
        "John"
      )
    assertEquals(string, "Hello, John!(en)")
  }
end ResourcesBundleResolverTest
