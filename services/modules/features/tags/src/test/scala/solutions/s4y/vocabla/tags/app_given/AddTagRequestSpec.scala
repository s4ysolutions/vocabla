package solutions.s4y.vocabla.tags.app_given

import solutions.s4y.vocabla.domain.model.Identifier
import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.app_given.ports.TagRequest
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.Fixture.makeTagRepositoryLayer
import zio.{Scope, ZIO}
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assertTrue}

object AddTagRequestSpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = suite("AddTag"){
    test("AddTag should add and return the one tag") {
      val ownerId = 1.identifier[Owner]
      val tag = Tag("tag1")
      val request: TagRequest.AddTag = TagRequest.AddTag(ownerId, tag)
      val useCase = summon[TagUseCase[TagRequest.AddTag, Identifier[Tag]]]
      for {
        tagId <- useCase(request)
        repository <- ZIO.service[TagRepository]
        tags <- repository.get(ownerId)
      } yield assertTrue(
        tags.size == 1,
        tags.exists(t => t.e.label == "tag1" && t.id == tagId)
      )
    }.provide(makeTagRepositoryLayer())
  }
}
