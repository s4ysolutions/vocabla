import type {InfraError} from '../app-repo/InfraError.ts';
import {AppError} from '../app-ports/errors/AppError.ts';
import loglevel from 'loglevel';

const log = loglevel.getLogger('infra2appError')

const infra2appError = (error: InfraError): AppError => {
  log.error(`Converting InfraError to AppError: ${error.message}`, error)
  return AppError(error.message)
}

export default infra2appError
