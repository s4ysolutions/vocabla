package solutions.s4y.vocabla.endpoint.http.routes

import zio.http.codec.PathCodec

val prefix: PathCodec[Unit] = PathCodec("rest") / "v1"
