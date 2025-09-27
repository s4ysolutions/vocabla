import {Brand} from 'effect';
import type {Identifier} from './identity/Identifier.ts';

type Brand = 'Student'

export type Student = {
  readonly nickname: string,
} & Brand.Brand<Brand>

export type StudentId = Identifier<Student>
