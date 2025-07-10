package solutions.s4y.vocabla.words.domain.model

case class Entry[ID](
    override val id: ID,
    word: ID,
    definitions: Seq[Definition],
    tags: Seq[ID],
    ownerId: ID
) extends Model[ID]:
  override def toString: String =
    s"Entry: $word, Definitions: ${definitions.mkString(", ")}, Tags: ${tags.mkString(", ")}"
