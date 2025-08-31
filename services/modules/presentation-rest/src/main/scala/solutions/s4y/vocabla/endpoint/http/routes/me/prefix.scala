package solutions.s4y.vocabla.endpoint.http.routes.me

import solutions.s4y.vocabla.endpoint.http.routes
import zio.http.codec.PathCodec

val prfix: PathCodec[Unit] = routes.prefix / "me"
