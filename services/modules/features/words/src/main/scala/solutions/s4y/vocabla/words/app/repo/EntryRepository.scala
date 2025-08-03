package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.tags.domain.Tag
import solutions.s4y.vocabla.words.domain.model.{Entry, Owner}
import zio.IO
import zio.stream.ZStream

trait EntryRepository:
  def put(
      ownerId: Identifier[Owner],
      entry: Entry
  ): IO[String, Identifier[Entry]]

  def get(
      entryId: Identifier[Entry]
  ): IO[String, Option[Entry]]

  def getForOwner(
      owner: Identifier[Owner]
  ): ZStream[Any, String, Identified[Entry]]
  
  def getForTag(
      tagId: Identifier[Tag]
  ): ZStream[Any, String, Identified[Entry]]

  def addTag(
      entryId: Identifier[Entry],
      tagId: Identifier[Tag]
  ): IO[String, Unit]

  def removeTag(
      entryId: Identifier[Entry],
      tagId: Identifier[Tag]
  ): IO[String, Unit]
