import * as Effect from "effect/Effect";
import * as Layer from "effect/Layer";
import * as Context from "effect/Context";

// Example: Another service that you might add later
interface UserService {
  getCurrentUser(): Effect.Effect<{ id: string; name: string }, string, never>;
}

class UserServiceTag extends Context.Tag("UserService")<
  UserServiceTag,
  UserService
>() {}

const userServiceImpl: UserService = {
  getCurrentUser: () =>
    Effect.succeed({ id: "user123", name: "John Doe" })
};

const UserServiceLayer = Layer.succeed(UserServiceTag, userServiceImpl);

// Example use case that requires UserService
const getCurrentUserUseCase = (): Effect.Effect<
  { id: string; name: string },
  string,
  UserServiceTag
> =>
  Effect.flatMap(UserServiceTag, (userService) =>
    userService.getCurrentUser()
  );

// Example use case that requires NO dependencies
const simpleUseCase = (): Effect.Effect<string, never, never> =>
  Effect.succeed("Hello World!");

// Export examples for testing
export {
  UserServiceLayer,
  getCurrentUserUseCase,
  simpleUseCase,
  UserServiceTag
};
