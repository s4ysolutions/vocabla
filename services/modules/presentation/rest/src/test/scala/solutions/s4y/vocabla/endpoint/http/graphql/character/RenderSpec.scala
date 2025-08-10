package solutions.s4y.vocabla.endpoint.http.graphql.character

import zio.test.*

object RenderSpec extends ZIOSpecDefault {

  def spec = suite("GraphQL")(
    test("rendering works") {
      assertTrue(api.render == """schema {
                                 |  query: Queries
                                 |}
                                 |
                                 |type Character {
                                 |  name: String!
                                 |  age: Int!
                                 |}
                                 |
                                 |type Queries {
                                 |  characters: [Character!]!
                                 |  character(name: String!): Character
                                 |}""".stripMargin)
    }
  )
}
