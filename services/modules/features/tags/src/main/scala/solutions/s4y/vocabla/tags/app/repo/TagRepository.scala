package solutions.s4y.vocabla.tags.app.repo

import solutions.s4y.vocabla.domain.model.{Identified, Identifier}
import solutions.s4y.vocabla.tags.domain.model.{Owner, Tag}
import zio.{Chunk, IO}

trait TagRepository:
  def add(tag: Tag): IO[String, Identifier[Tag]]
  def get(owner: Identifier[Owner]): IO[String, Chunk[Identified[Tag]]]
