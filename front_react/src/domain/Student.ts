import {Brand} from 'effect';

type Brand = 'Student'

export type Student = {
  readonly nickname: string,
} & Brand.Brand<Brand>

