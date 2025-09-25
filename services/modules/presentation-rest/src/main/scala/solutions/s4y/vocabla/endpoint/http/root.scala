package solutions.s4y.vocabla.endpoint.http

import zio.http.{Body, Handler, Header, Headers, MediaType, Request, Response, Status}

private val rootHandler: Handler[Any, Nothing, Request, Response] =
  Handler.fromFunction { _ =>
    val html =
      """<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Vocabla API</title>
  <style>
      body {
          font-family: Arial, sans-serif;
          max-width: 800px;
          margin: 50px auto;
          padding: 20px;
          background-color: #f5f5f5;
      }
      .container {
          background: white;
          padding: 40px;
          border-radius: 8px;
          box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      }
      h1 { color: #333; text-align: center; }
      .links { margin: 30px 0; }
      .link-item {
          display: block;
          padding: 15px;
          margin: 10px 0;
          background: #007bff;
          color: white;
          text-decoration: none;
          border-radius: 5px;
          text-align: center;
          transition: background-color 0.3s;
      }
      .link-item:hover { background: #0056b3; }
      .description { color: #666; margin: 20px 0; text-align: center; }
  </style>
</head>
<body>
  <div class="container">
      <h1>Vocabla API</h1>
      <p class="description">
          Welcome to the Vocabla vocabulary learning API.
          Use the links below to explore the API documentation and endpoints.
      </p>
      <div class="links">
          <a href="/swagger-ui" class="link-item">
              📚 API Documentation (Swagger UI)
          </a>
          <a href="/rest/v1/ping?message=hello" class="link-item">
              🏓 Test Ping Endpoint
          </a>
      </div>
      <p class="description">
          <small>API Version: 1.0.0</small>
      </p>
  </div>
</body>
</html>"""

    Response(
      status = Status.Ok,
      headers = Headers(Header.ContentType(MediaType.text.html)),
      body = Body.fromString(html)
    )
  }
