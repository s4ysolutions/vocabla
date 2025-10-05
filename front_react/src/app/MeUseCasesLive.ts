import type {MeUseCases} from '../app-ports/me/MeUseCases.ts';
import {Effect} from 'effect';
import type {StudentId} from '../domain/Student.ts';
import {type AsyncData, SuccessData} from '../app-ports/types.ts';
import type {AppError} from '../app-ports/errors/AppError.ts';

class MeUseCasesLive implements MeUseCases {
  get lastStudentId(): Effect.Effect<AsyncData<StudentId, AppError>> {
    return Effect.succeed(SuccessData<StudentId, AppError>(1));
  }
}
export default MeUseCasesLive;
