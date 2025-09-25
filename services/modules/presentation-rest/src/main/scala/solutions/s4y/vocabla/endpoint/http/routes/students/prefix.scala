package solutions.s4y.vocabla.endpoint.http.routes.students

import solutions.s4y.vocabla.endpoint.http.routes
import zio.http.codec.PathCodec

val prefix: PathCodec[Unit] = routes.prefix / "students"
