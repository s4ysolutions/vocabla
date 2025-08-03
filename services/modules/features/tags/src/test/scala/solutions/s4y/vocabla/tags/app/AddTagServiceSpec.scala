package solutions.s4y.vocabla.tags.app

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.logging.consoleColorTraceLogger
import solutions.s4y.vocabla.tags.app.ports.AddTagUseCase
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.Fixture.makeTagRepositoryLayer
import zio.test.Assertion.equalTo
import zio.test.{Spec, TestEnvironment, ZIOSpecDefault, assert}
import zio.{Scope, ZIO, ZLayer}

object AddTagServiceSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment & Scope, Any] = suite("AddTag") {
    test("AddTag should add and return the one tag") {
      val ownerId = 1.identifier[Owner]
      val tag = Tag("tag1")
      for {
        tagId <- ZIO.serviceWithZIO[AddTagUseCase](_.addTag(ownerId, tag))
        repository <- ZIO.service[TagRepository]
        tags <- repository.get(ownerId)
        _ = assert(tagId)(equalTo(11.identifier[Tag]))
      } yield assert(tags.size)(equalTo(1))
    }.provide({
      val repository = makeTagRepositoryLayer()
      repository >>> TagService.layer ++ repository ++ consoleColorTraceLogger
    })
  }
}
