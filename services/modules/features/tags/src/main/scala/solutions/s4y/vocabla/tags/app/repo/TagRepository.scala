package solutions.s4y.vocabla.tags.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.tags.domain.{Owner, Tag}
import zio.{Chunk, IO}

trait TagRepository:
  def add(ownerId: Identifier[Owner], tag: Tag): IO[String, Identifier[Tag]]
  def remove(
      ownerId: Identifier[Owner],
      tagId: Identifier[Tag]
  ): IO[String, Boolean]

  def get(ownerId: Identifier[Owner]): IO[String, Chunk[Identified[Tag]]]
