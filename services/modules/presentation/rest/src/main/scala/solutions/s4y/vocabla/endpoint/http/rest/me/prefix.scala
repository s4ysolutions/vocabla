package solutions.s4y.vocabla.endpoint.http.rest.me

import solutions.s4y.vocabla.endpoint.http.rest
import zio.http.codec.PathCodec

val me: PathCodec[Unit] = rest.prefix / "me"
