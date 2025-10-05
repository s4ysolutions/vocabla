import type {AsyncData} from '../types.ts';
import type {StudentId} from '../../domain/Student.ts';
import type {AppError} from '../errors/AppError.ts';
import {Context, Effect} from 'effect';

export interface MeUseCases {
  currentStudentId: Effect.Effect<AsyncData<StudentId, AppError>>
}

export class MeUseCasesTag extends Context.Tag('MeUseCasesTag')<MeUseCasesTag, MeUseCases>() {
}
