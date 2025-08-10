package solutions.s4y.vocabla.endpoint.http.graphql.character

case class CharacterName(name: String)
case class Queries(
    characters: List[Character],
    character: CharacterName => Option[Character]
)
