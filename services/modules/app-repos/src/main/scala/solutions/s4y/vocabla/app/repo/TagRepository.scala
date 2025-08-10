package solutions.s4y.vocabla.app.repo

import solutions.s4y.vocabla.domain.Tag
import solutions.s4y.vocabla.domain.identity.{Identified, Identifier}
import zio.IO

trait TagRepository:
  def create(
      tag: Tag
  ): IO[String, Identifier[Tag]]
  def update(tag: Identified[Tag]): IO[String, Boolean]
  def delete(tagId: Identifier[Tag]): IO[String, Boolean]
  def get(tagId: Identifier[Tag]): IO[String, Option[Tag]]
