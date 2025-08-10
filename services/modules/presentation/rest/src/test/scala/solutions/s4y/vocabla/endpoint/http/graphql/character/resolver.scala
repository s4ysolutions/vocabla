package solutions.s4y.vocabla.endpoint.http.graphql.character

val queries = Queries(getCharacters, args => getCharacter(args.name))
