import {type MeUseCases, MeUseCasesTag} from '../app-ports/MeUseCases.ts';
import {Effect, Layer} from 'effect';
import type {StudentId} from '../domain/Student.ts';
import {type AsyncData, SuccessData} from '../app-ports/types.ts';
import type {AppError} from '../app-ports/errors/AppError.ts';

class MeUseCasesLive implements MeUseCases {
  readonly currentStudentId: Effect.Effect<AsyncData<StudentId, AppError>> =
    Effect.succeed(SuccessData<StudentId, AppError>(1));

  static make(): Effect.Effect<MeUseCasesLive> {
    return Effect.succeed(new MeUseCasesLive());
  }

  static readonly layer: Layer.Layer<MeUseCasesTag> = Layer.effect(MeUseCasesTag, MeUseCasesLive.make());
}

export default MeUseCasesLive
