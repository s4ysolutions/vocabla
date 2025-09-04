interface Error {
  statusText: string;
  body?: unknown;
  bodyReadError?: unknown;
  __brand?: 'HTTPError';
}

type BadRequestError400 = Error & { status: 400 }
type UnauthorizedError401 = Error & { status: 401 }
type ForbiddenError403 = Error & { status: 403 }
type NotFoundError404 = Error & { status: 404 }
type ConflictError409 = Error & { status: 409 }
type InternalServerError500 = Error & { status: 500 }
type HTTPError =
  | BadRequestError400
  | UnauthorizedError401
  | ForbiddenError403
  | NotFoundError404
  | ConflictError409
  | InternalServerError500

export type {
  HTTPError,
  BadRequestError400,
  UnauthorizedError401,
  ForbiddenError403,
  NotFoundError404,
  ConflictError409,
  InternalServerError500
}
