package solutions.s4y.vocabla.infra.mvstore

import solutions.s4y.vocabla.app.repo.TagRepository
import solutions.s4y.vocabla.domain.identity.Identified
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Student, Tag}
import solutions.s4y.vocabla.infra.mvstore.Fixture.makeTagRepositoryLayer
import zio.test.*
import zio.test.Assertion.{equalTo, isNone, isSome, isTrue}
import zio.{Scope, ZIO, ZLayer}

object TagRepositoryMVStoreSpec extends ZIOSpecDefault {
  override def spec: Spec[TestEnvironment & Scope, Any] = suite("TagRepositoryMVStoreSpec")(
    test("create should add and get a tag") {
      val ownerId = 1.identifier[Student]
      val tag = Tag("tag1", ownerId)
      for {
        repo <- ZIO.service[TagRepository]
        createdId <- repo.create(tag)
        fetched <- repo.get(createdId)
      } yield assert(fetched)(isSome(equalTo(Tag("tag1", ownerId))))
    },
    test("update should modify an existing tag") {
      val ownerId = 2.identifier[Student]
      val tag = Tag("tag2", ownerId)
      for {
        repo <- ZIO.service[TagRepository]
        createdId <- repo.create(tag)
        updated <- repo.update(Identified(createdId, Tag("tag2-updated", ownerId)))
        fetched <- repo.get(createdId)
      } yield assert(updated)(isTrue) && assert(fetched)(isSome(equalTo(Tag("tag2-updated", ownerId))))
    },
    test("delete should remove a tag") {
      val ownerId = 3.identifier[Student]
      val tag = Tag("tag3", ownerId)
      for {
        repo <- ZIO.service[TagRepository]
        createdId <- repo.create(tag)
        deleted <- repo.delete(createdId)
        fetched <- repo.get(createdId)
      } yield assert(deleted)(isTrue) && assert(fetched)(isNone)
    },
    test("get should return None for non-existent tag") {
      val nonExistentId = 999.identifier[Tag]
      for {
        repo <- ZIO.service[TagRepository]
        fetched <- repo.get(nonExistentId)
      } yield assert(fetched)(isNone)
    }
  ).provide(makeTagRepositoryLayer())
}
