package solutions.s4y.vocabla.words.domain.model

import solutions.s4y.vocabla.domain.model.Identifier

case class Entry(
                  headword: Headword,
                  definitions: Seq[Definition],
                  tags: Seq[Identifier[Tag]],
                  ownerId: Identifier[Owner]
):
  override def toString: String =
    s"Entry: $headword, Definitions: ${definitions.mkString(", ")}, Tags: ${tags.mkString(", ")}"
