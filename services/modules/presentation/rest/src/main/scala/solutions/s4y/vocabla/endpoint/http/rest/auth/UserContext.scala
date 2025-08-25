package solutions.s4y.vocabla.endpoint.http.rest.auth

import solutions.s4y.vocabla.domain.User
import solutions.s4y.vocabla.domain.identity.Identifier

case class UserContext(id: Identifier[User], user: User);
