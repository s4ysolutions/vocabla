package solutions.s4y.vocabla.me.domain.model

import solutions.s4y.vocabla.tags.domain.Owner as TagsOwner
import solutions.s4y.vocabla.words.domain.model.Owner as WordsOwner

trait Me

object Me:
  extension (me: Me)
    def asTagsOwner: TagsOwner =
      me.asInstanceOf[TagsOwner]

    def asWordsOwner: WordsOwner =
      me.asInstanceOf[WordsOwner]
