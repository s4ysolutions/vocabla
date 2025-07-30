package solutions.s4y.vocabla.tags.infra.mvstore

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.model.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.Fixture.layerTestRepository
import zio.test.*
import zio.{Chunk, Scope, ZIO, ZLayer}

object MVStoreTagRepositorySpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("add should create a new tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.add(Tag("test-tag", 1.identifier[Owner]))
          tags <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          tagId == 11.identifier[Tag],
          tags.size == 1,
          tags.head.e.label == "test-tag",
          tags.head.id == 11.identifier[Tag]
        )
      },
      test("get should return all tags for specific owner") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId1 <- repository.add(Tag("tag1", 1.identifier[Owner]))
          tagId2 <- repository.add(Tag("tag2", 1.identifier[Owner]))
          tagId3 <- repository.add(Tag("tag3", 2.identifier[Owner]))
          tags <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t =>
            t.id == 11
              .identifier[
              Tag
            ] && t.id == tagId1 && t.e.label == "tag1"
          ),
          tags.exists(t =>
            t.id == 12.identifier && t.id == tagId2 && t.e.label == "tag2"
          ),
          !tags.exists(t => t.id == tagId3)
        )
      },
      test("get should return empty chunk when owner has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository]
          tags <- repository.get(999.identifier[Owner])
        } yield assertTrue(
          tags.isEmpty
        )
      }
    ).provide(layerTestRepository),
  )
}
