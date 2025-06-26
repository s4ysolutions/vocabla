package solutions.s4y.vocabla.words.domain.model

case class Entry(
    id: Entry.Id,
    word: Word.Id,
    definitions: Seq[Definition],
    tags: Seq[Tag.Id],
    ownerId: Owner.Id
):
  override def toString: String =
    s"Entry: $word, Definitions: ${definitions.mkString(", ")}, Tags: ${tags.mkString(", ")}"

object Entry:
  type Id = Entity.Id
