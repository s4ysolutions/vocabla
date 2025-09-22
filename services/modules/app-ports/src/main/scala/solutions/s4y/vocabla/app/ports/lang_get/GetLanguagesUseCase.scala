package solutions.s4y.vocabla.app.ports.lang_get

import zio.UIO

trait GetLanguagesUseCase {
  def apply(): UIO[GetLanguagesResponse]
}
