package solutions.s4y.vocabla.endpoint.http.routes.tags

import solutions.s4y.vocabla.endpoint.http.routes
import zio.http.codec.PathCodec

val prefix: PathCodec[Unit] = routes.prefix / "tags"
