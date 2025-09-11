import {identifierFromNumber} from '../transformers/identifierFromNumber.ts';
import type {Student} from '../../../../domain/Student.ts';

export const identifierFromNumberStudent = identifierFromNumber<Student>()
