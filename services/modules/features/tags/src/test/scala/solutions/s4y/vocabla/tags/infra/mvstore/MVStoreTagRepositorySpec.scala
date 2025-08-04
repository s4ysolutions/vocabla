package solutions.s4y.vocabla.tags.infra.mvstore

import solutions.s4y.vocabla.domain.model.Identifier.identifier
import solutions.s4y.vocabla.tags.app.repo.TagRepository
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import solutions.s4y.vocabla.tags.infra.mvstore.Fixture.makeTagRepositoryLayer
import zio.test.*
import zio.{Chunk, Scope, ZIO, ZLayer}

object MVStoreTagRepositorySpec extends ZIOSpecDefault {
  def spec: Spec[TestEnvironment & Scope, Any] = suite("MVStoreRepositories")(
    suite("TagRepository")(
      test("set should create and get tags for an ownerId") {
        for {
          repository <- ZIO.service[TagRepository]
          tag1 <- repository.create(1.identifier[Owner], Tag("tag1"))
          tag2 <- repository.create(1.identifier[Owner], Tag("tag2"))
          tags <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          tags.size == 2,
          tags.exists(t => t.e.label == "tag1" && t.id == 11.identifier[Tag]),
          tags.exists(t => t.e.label == "tag2" && t.id == 12.identifier[Tag])
        )
      },
      test("get should return empty chunk when ownerId has no tags") {
        for {
          repository <- ZIO
            .service[TagRepository]
          tags <- repository.get(999.identifier[Owner])
        } yield assertTrue(
          tags.isEmpty
        )
      },
      test("delete should remove a tag") {
        for {
          repository <- ZIO.service[TagRepository]
          tagId <- repository.create(1.identifier[Owner], Tag("tagToDelete"))
          deleted <- repository.delete(tagId)
          tagsAfterDelete <- repository.get(1.identifier[Owner])
        } yield assertTrue(
          deleted,
          tagsAfterDelete.isEmpty
        )
      }
    ).provide(makeTagRepositoryLayer())
  )
}
