package solutions.s4y.vocabla.app.macros
import scala.quoted.*
import zio.{NonEmptyChunk, ZIO}
import solutions.s4y.vocabla.domain.{AuthorizationService, UserContext}
import solutions.s4y.vocabla.app.ports.errors.NotAuthorized

object AuthMacros:
  inline def can[T](
      inline authMethod: (T, UserContext) => Either[NonEmptyChunk[
        String
      ], Unit],
      inline resource: T
  ): ZIO[UserContext, NotAuthorized, Unit] =
    ${ canImpl('authMethod, 'resource) }

  def canImpl[T: Type](
      authMethod: Expr[(T, UserContext) => Either[NonEmptyChunk[String], Unit]],
      resource: Expr[T]
  )(using Quotes): Expr[ZIO[UserContext, NotAuthorized, Unit]] =
    '{
      ZIO.serviceWithZIO[UserContext](userContext =>
        $authMethod($resource, userContext).fold(
          (errors: NonEmptyChunk[String]) => ZIO.fail(NotAuthorized(errors)),
          _ => ZIO.unit
        )
      )
    }
