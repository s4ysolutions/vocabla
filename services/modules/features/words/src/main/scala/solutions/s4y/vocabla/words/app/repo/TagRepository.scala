package solutions.s4y.vocabla.words.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.words.domain.model.{Owner, Tag}
import zio.{Chunk, IO}

trait TagRepository:
  def addTag(
      owner: Identifier[Owner],
      label: String
  ): IO[String, Identifier[Tag]]

  def getTagsForOwner(
      owner: Identifier[Owner]
  ): IO[String, Chunk[Identified[Tag]]]
