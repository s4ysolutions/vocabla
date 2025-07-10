package solutions.s4y.vocabla.endpoint.http.rest.words

import solutions.s4y.vocabla.endpoint.http.rest
import zio.http.codec.PathCodec

val prefix: PathCodec[Unit] = rest.prefix / "words"
