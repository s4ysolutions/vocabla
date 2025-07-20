package solutions.s4y.vocabla.endpoint.http.codecs
/*
trait IdCodec[ID] {
  def string2id(string: String): Either[String, ID]
}

object IdCodec {
  given IdCodec[String] with
    def string2id(string: String): Either[String, String] = Right(string)

  given IdCodec[Long] with
    def string2id(string: String): Either[String, Long] = try
      Right(string.toLong)
    catch {
      case _: NumberFormatException => Left(s"Invalid Long: $string")
    }

  extension (string: String)
    def toID[ID](using codec: IdCodec[ID]): Either[String, ID] =
      codec.string2id(string)
}
 */
