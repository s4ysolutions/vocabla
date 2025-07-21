package solutions.s4y.vocabla.endpoint.http.schema

import zio.schema.Schema

given [A: Schema](using listSchema: Schema[List[A]]): Schema[Seq[A]] =
  listSchema.transform(
    (list: List[A]) => list.toSeq,
    (seq: Seq[A]) => seq.toList
  )
