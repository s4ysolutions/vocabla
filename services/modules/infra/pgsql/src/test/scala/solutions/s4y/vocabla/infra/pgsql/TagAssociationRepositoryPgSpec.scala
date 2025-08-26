package solutions.s4y.vocabla.infra.pgsql

import solutions.s4y.vocabla.app.repo.{
  EntryRepository,
  TagAssociationRepository,
  TagRepository
}
import solutions.s4y.vocabla.app.repo.tx.TransactionManager
import solutions.s4y.vocabla.domain.identity.Identifier.identifier
import solutions.s4y.vocabla.domain.{Definition, Entry, Headword, Student, Tag}
import solutions.s4y.zio.consoleColorDebugLogger
import zio.{Chunk, Scope, ZIO}
import zio.test.{Spec, TestAspect, TestEnvironment, ZIOSpecDefault, assertTrue}

object TagAssociationRepositoryPgSpec extends ZIOSpecDefault {

  override def spec: Spec[TestEnvironment & Scope, Any] =
    suite("TagAssociationRepositoryPgSpec")(
      suite("Tag-Entry Associations")(
        test("associate tag with entry") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            result <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create a tag and entry first
                tagId <- tagRepo.create(Tag("Test Tag", 1L.identifier[Student]))
                entryId <- entryRepo.create(
                  Entry(
                    Headword("Test Entry", "en"),
                    Chunk(Definition("Test Definition", "en")),
                    1L.identifier[Student]
                  )
                )

                // Associate tag with entry
                success <- assocRepo.associateTagWithEntry(tagId, entryId)
              } yield success
            }
          } yield assertTrue(result)
        },
        test("get tags for entry") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            tags <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create tag and entry
                tagId <- tagRepo.create(Tag("Test Tag", 1L.identifier[Student]))
                entryId <- entryRepo.create(
                  Entry(
                    Headword("Test Entry", "en"),
                    Chunk(Definition("Test Definition", "en")),
                    1L.identifier[Student]
                  )
                )

                // Associate tag with entry
                _ <- assocRepo.associateTagWithEntry(tagId, entryId)

                // Get tags for entry
                tags <- assocRepo.getTags(entryId)
              } yield tags
            }
          } yield assertTrue(
            tags.length == 1 &&
              tags.head == 1L.identifier[Tag]
          )
        },
        test("get tagged entries for tag") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            entryIds <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create tag and entry
                tagId <- tagRepo.create(Tag("Test Tag", 1L.identifier[Student]))
                entryId <- entryRepo.create(
                  Entry(
                    Headword("Test Entry", "en"),
                    Chunk(Definition("Test Definition", "en")),
                    1L.identifier[Student]
                  )
                )

                // Associate tag with entry
                _ <- assocRepo.associateTagWithEntry(tagId, entryId)

                // Get tagged entries
                entryIds <- assocRepo.getTagged(tagId)
              } yield entryIds
            }
          } yield assertTrue(
            entryIds.length == 1 && entryIds.head == 1.identifier[Entry]
          )
        },
        test("disassociate tag from entry") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            result <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create and associate
                tagId <- tagRepo.create(Tag("Test Tag", 1L.identifier[Student]))
                entryId <- entryRepo.create(
                  Entry(
                    Headword("Test Entry", "en"),
                    Chunk(Definition("Test Definition", "en")),
                    1L.identifier[Student]
                  )
                )
                _ <- assocRepo.associateTagWithEntry(tagId, entryId)

                // Disassociate
                success <- assocRepo.disassociateTagFromEntry(tagId, entryId)

                // Verify no tags remain
                tags <- assocRepo.getTags(entryId)
              } yield (success, tags.isEmpty)
            }
          } yield assertTrue(result._1 && result._2)
        },
        test("disassociate tag from all entries") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            result <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create tag and multiple entries
                tagId <- tagRepo.create(Tag("Test Tag", 1L.identifier[Student]))
                entryId1 <- entryRepo.create(
                  Entry(
                    Headword("Test Entry 1", "en"),
                    Chunk(Definition("Test Definition 1", "en")),
                    1L.identifier[Student]
                  )
                )
                entryId2 <- entryRepo.create(
                  Entry(
                    Headword("Test Entry 2", "en"),
                    Chunk(Definition("Test Definition 2", "en")),
                    1L.identifier[Student]
                  )
                )

                // Associate tag with both entries
                _ <- assocRepo.associateTagWithEntry(tagId, entryId1)
                _ <- assocRepo.associateTagWithEntry(tagId, entryId2)

                // Disassociate tag from all
                success <- assocRepo.disassociateTagFromAll(tagId)

                // Verify no tagged entries remain
                taggedEntries <- assocRepo.getTagged(tagId)
              } yield (success, taggedEntries.isEmpty)
            }
          } yield assertTrue(result._1 && result._2)
        },
        test("disassociate entry from all tags") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            result <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create multiple tags and one entry
                tagId1 <- tagRepo.create(
                  Tag("Test Tag 1", 1L.identifier[Student])
                )
                tagId2 <- tagRepo.create(
                  Tag("Test Tag 2", 1L.identifier[Student])
                )
                entryId <- entryRepo.create(
                  Entry(
                    Headword("Test Entry", "en"),
                    Chunk(Definition("Test Definition", "en")),
                    1L.identifier[Student]
                  )
                )

                // Associate both tags with entry
                _ <- assocRepo.associateTagWithEntry(tagId1, entryId)
                _ <- assocRepo.associateTagWithEntry(tagId2, entryId)

                // Disassociate entry from all tags
                success <- assocRepo.disassociateTaggedFromAll(entryId)

                // Verify no tags remain for entry
                tags <- assocRepo.getTags(entryId)
              } yield (success, tags.isEmpty)
            }
          } yield assertTrue(result._1 && result._2)
        },
        test("multiple associations work correctly") {
          for {
            transactionManager <- ZIO.service[TransactionManager]
            result <- transactionManager.transaction {
              for {
                tagRepo <- ZIO.service[TagRepository]
                entryRepo <- ZIO.service[EntryRepository]
                assocRepo <- ZIO.service[TagAssociationRepository[Entry]]

                // Create 2 tags and 2 entries
                tagId1 <- tagRepo.create(Tag("Tag 1", 1L.identifier[Student]))
                tagId2 <- tagRepo.create(Tag("Tag 2", 1L.identifier[Student]))
                entryId1 <- entryRepo.create(
                  Entry(
                    Headword("Entry 1", "en"),
                    Chunk(Definition("Definition 1", "en")),
                    1L.identifier[Student]
                  )
                )
                entryId2 <- entryRepo.create(
                  Entry(
                    Headword("Entry 2", "en"),
                    Chunk(Definition("Definition 2", "en")),
                    1L.identifier[Student]
                  )
                )

                // Create cross associations: tag1->entry1, tag1->entry2, tag2->entry1
                _ <- assocRepo.associateTagWithEntry(tagId1, entryId1)
                _ <- assocRepo.associateTagWithEntry(tagId1, entryId2)
                _ <- assocRepo.associateTagWithEntry(tagId2, entryId1)

                // Test various queries
                entry1Tags <- assocRepo.getTags(entryId1) // Should have 2 tags
                entry2Tags <- assocRepo.getTags(entryId2) // Should have 1 tag
                tag1Entries <- assocRepo.getTagged(
                  tagId1
                ) // Should have 2 entries
                tag2Entries <- assocRepo.getTagged(
                  tagId2
                ) // Should have 1 entry

              } yield (entry1Tags, entry2Tags, tag1Entries, tag2Entries)
            }
          } yield assertTrue(
            result._1.length == 2 && // entry1 has 2 tags
              result._2.length == 1 && // entry2 has 1 tag
              result._3.length == 2 && // tag1 has 2 entries
              result._4.length == 1 // tag2 has 1 entry
          )
        }
      )
    ).provideLayer(
      consoleColorDebugLogger >>> Fixture.layerWithTagAssociationRepository
    ) @@ TestAspect.before(Fixture.testSystem) @@ TestAspect.sequential
}
