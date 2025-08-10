package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.domain.Entry
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import zio.IO

trait EntryRepository:
  def createEntry(
      entry: Entry
  ): IO[String, Identifier[Entry]]

  def readEntry(
      entryId: Identifier[Entry]
  ): IO[String, Option[Entry]]

  def updateEntry(
      entry: Identified[Entry]
  ): IO[String, Boolean]

  def deleteEntry(
      entryId: Identifier[Entry]
  ): IO[String, Boolean]
