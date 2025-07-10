package solutions.s4y.vocabla.endpoint.http.graphql.character

case class Character(name: String, age: Int)

private val characters: List[Character] = List(
  Character("Alice", 30),
  Character("Bob", 25),
  Character("Charlie", 35)
)
/*
def getCharacters: List[Character] = characters
def getCharacter(name: String): Option[Character] =
  characters.find(ch => ch.name == name)
 */ 
def getCharacters: List[Character] = characters
def getCharacter(name: String): Option[Character] =
  characters.find(ch => ch.name == name)
