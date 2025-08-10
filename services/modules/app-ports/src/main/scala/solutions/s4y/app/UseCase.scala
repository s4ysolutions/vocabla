package solutions.s4y.app

trait UseCase[Command <: AppCommand]:
  def apply(command: Command): command.Result
