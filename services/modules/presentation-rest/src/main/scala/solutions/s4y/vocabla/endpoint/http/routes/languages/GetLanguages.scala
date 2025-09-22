package solutions.s4y.vocabla.endpoint.http.routes.languages

import solutions.s4y.vocabla.app.ports.lang_get.{GetLanguagesResponse, GetLanguagesUseCase}
import solutions.s4y.vocabla.endpoint.http.error.HttpError
import solutions.s4y.vocabla.endpoint.http.routes.prefix
import zio.ZIO
import zio.http.codec.Doc
import zio.http.endpoint.AuthType.None
import zio.http.endpoint.Endpoint
import zio.http.{Method, Response, Route, Status}

object GetLanguages:
  val endpoint: Endpoint[
    Unit,
    Unit,
    HttpError.InternalServerError500,
    GetLanguagesResponse,
    None
  ] =
    Endpoint(
      (Method.GET / prefix / "languages") ?? Doc.p(
        "Get all available languages with their codes, names, and flags"
      )
    )
      .tag("Languages")
      .out[GetLanguagesResponse](Doc.p("List of all available languages"))
      .outError[HttpError.InternalServerError500](Status.InternalServerError)
      ?? Doc.p("Endpoint to retrieve all supported languages")

  val route: Route[GetLanguagesUseCase, Response] =
    endpoint.implement(_ =>
      ZIO.serviceWithZIO[GetLanguagesUseCase](_())
    )
