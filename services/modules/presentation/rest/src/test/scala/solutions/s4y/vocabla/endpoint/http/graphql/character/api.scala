package solutions.s4y.vocabla.endpoint.http.graphql.character

import caliban.schema.ArgBuilder.auto.*
import caliban.schema.Schema.auto.*
import caliban.{RootResolver, graphQL}

val api = graphQL(RootResolver(queries))
