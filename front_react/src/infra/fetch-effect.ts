import { Effect } from "effect";

const proto = "http";
const host = "localhost";
const port = 8080;
const baseUrl = `${proto}://${host}:${port}`;
const prefix = "/rest/v1";
const apiUrl = `${baseUrl}${prefix}`;

const prettyPrintError = (err: unknown, indent = 0): string => {
  const prefix = "  ".repeat(indent);

  if (!(err instanceof Error)) {
    return prefix + String(err);
  }

  let result = `${prefix}${err.name}: ${err.message ?? String(err)}`;

  if (err instanceof AggregateError && Array.isArray(err.errors)) {
    for (const innerErr of err.errors) {
      result += "\n" + prettyPrintError(innerErr, indent + 1);
    }
  }

  if ("cause" in err && err.cause instanceof Error) {
    result +=
      "\n" + prefix + "Caused by:\n" + prettyPrintError(err.cause, indent + 1);
  }

  return result;
};

const fetchResult = <B>(
  method: "GET" | "POST" | "PUT" | "DELETE",
  uri: string,
  body?: B
): Effect.Effect<Response, string, never> =>
  Effect.tryPromise(() =>
    fetch(`${apiUrl}${uri}`, {
      method,
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
      },
      body: body ? JSON.stringify(body) : undefined,
    })
  ).pipe(
    Effect.tapError((error) =>
      Effect.logError("fetchResultEffect failed: " + prettyPrintError(error))
    ),
    Effect.mapError((error) => `Failed to call fetch: ${error.message}`)
  );

const handleHTTPError = (
  metod: "GET" | "POST" | "PUT" | "DELETE",
  uri: string,
  response: Response
): Effect.Effect<Response, string, never> =>
  Effect.gen(function* () {
    if (!response.ok) {
      const errorText = yield* convertResponseToJson<{ message: string }>(
        response
      )
      const errorMessage = errorText.message || "no error message in response";
      return yield* Effect.fail(
        `HTTP error (${response.status}) ${metod} ${uri}: ${errorMessage}`
      );
    }
    return response;
  });

const convertResponseToJson = <J>(
  response: Response
): Effect.Effect<J, string, never> =>
  Effect.tryPromise(() => response.json().then((json: J) => json as J)).pipe(
    Effect.tapError((error) =>
      Effect.logError("Failed to parse JSON: " + prettyPrintError(error))
    ),
    Effect.mapError((error) => `Failed to parse JSON: ${error.message}`)
  );

const transformJson = <J, R>(
  json: J,
  transform: (json: J) => R
): Effect.Effect<R, string, never> =>
  Effect.try(() => transform(json)).pipe(
    Effect.tapError((error) =>
      Effect.logError("Failed to transform JSON: " + prettyPrintError(error))
    ),
    Effect.mapError((error) => `Failed to transform JSON: ${error.message}`)
  );

const fetchJsonEffect = <B, J, R>(
  method: "GET" | "POST" | "PUT" | "DELETE",
  uri: string,
  transform: (json: J) => R,
  body?: B
): Effect.Effect<R, string, never> =>
  Effect.gen(function* () {
    const response = yield* fetchResult<B>(method, uri, body);
    yield* handleHTTPError(method, uri, response);
    const json = yield* convertResponseToJson<J>(response);
    const result = yield* transformJson<J, R>(json, transform);
    return result;
  });

export const post = <B, J, R>(
  uri: string,
  body: B,
  transform: (json: J) => R
): Effect.Effect<R, string, never> =>
  fetchJsonEffect<B, J, R>("POST", uri, transform, body);

export const get = <J, R>(
  uri: string,
  transform: (json: J) => R
): Effect.Effect<R, string, never> =>
  fetchJsonEffect<undefined, J, R>("GET", uri, transform);
