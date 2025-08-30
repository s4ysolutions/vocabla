package solutions.s4y.vocabla.endpoint.http.rest.error

import zio.NonEmptyChunk
import zio.schema.Schema

enum HttpError(val message: String | NonEmptyChunk[String]):
  case BadRequest400(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case NotAuthorized401(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case Forbidden403(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case NotFound404(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case Conflict409(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case Gone410(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case UnprocessableEntity422(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case InternalServerError500(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case NotImplemented501(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case ServiceUnavailable503(message: String | NonEmptyChunk[String])
      extends HttpError(message)
  case GatewayTimeout504(message: String | NonEmptyChunk[String])
      extends HttpError(message)

object NotAuthorized401:
  given Schema[NotAuthorized401] = Schema.derived

/*Schema.enumeration(
    "HttpError",
    NonEmptyChunk(
      Schema.Case("BadRequest400", Schema[String].transform(HttpError.BadRequest400(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.BadRequest400]),
      Schema.Case("NotAuthorized401", Schema[String].transform(HttpError.NotAuthorized401(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.NotAuthorized401]),
      Schema.Case("Forbidden403", Schema[NonEmptyChunk[String]].transform(HttpError.Forbidden403(_), _.message.asInstanceOf[NonEmptyChunk[String]]), _.isInstanceOf[HttpError.Forbidden403]),
      Schema.Case("NotFound404", Schema[String].transform(HttpError.NotFound404(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.NotFound404]),
      Schema.Case("Conflict409", Schema[String].transform(HttpError.Conflict409(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.Conflict409]),
      Schema.Case("Gone410", Schema[String].transform(HttpError.Gone410(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.Gone410]),
      Schema.Case("UnprocessableEntity422", Schema[NonEmptyChunk[String]].transform(HttpError.UnprocessableEntity422(_), _.message.asInstanceOf[NonEmptyChunk[String]]), _.isInstanceOf[HttpError.UnprocessableEntity422]),
      Schema.Case("InternalServerError500", Schema[String].transform(HttpError.InternalServerError500(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.InternalServerError500]),
      Schema.Case("NotImplemented501", Schema[String].transform(HttpError.NotImplemented501(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.NotImplemented501]),
      Schema.Case("ServiceUnavailable503", Schema[String].transform(HttpError.ServiceUnavailable503(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.ServiceUnavailable503]),
      Schema.Case("GatewayTimeout504", Schema[String].transform(HttpError.GatewayTimeout504(_), _.message.asInstanceOf[String]), _.isInstanceOf[HttpError.GatewayTimeout504])
    ),
    TypeId.parse("solutions.s4y.vocabla.endpoint.http.rest.error.HttpError")
  )*/
